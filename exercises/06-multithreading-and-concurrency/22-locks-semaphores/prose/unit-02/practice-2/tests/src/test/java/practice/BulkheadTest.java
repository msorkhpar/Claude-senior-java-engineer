package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BulkheadTest {

    /** Occupies one slot of {@code bulkhead} on another thread until {@link #finish()}. */
    private static final class Busy {
        private final CountDownLatch inside = new CountDownLatch(1);
        private final CountDownLatch done = new CountDownLatch(1);
        private final Thread thread;

        Busy(Bulkhead bulkhead) throws InterruptedException {
            thread = new Thread(() -> bulkhead.tryCall(() -> {
                inside.countDown();
                try {
                    return done.await(8, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }));
            thread.setDaemon(true);
            thread.start();
            assertThat(inside.await(3, TimeUnit.SECONDS)).as("the busy task is running").isTrue();
        }

        void finish() throws InterruptedException {
            done.countDown();
            thread.join(3_000);
        }
    }

    @Test
    void runsWhenASlotIsFree() {
        Bulkhead bulkhead = new Bulkhead(2);
        assertThat(bulkhead.tryCall(() -> "done")).contains("done");
        assertThat(bulkhead.available()).isEqualTo(2);
    }

    @Test
    void rejectsWhenFull() throws InterruptedException {
        Bulkhead bulkhead = new Bulkhead(1);
        Busy busy = new Busy(bulkhead);
        AtomicBoolean ran = new AtomicBoolean();
        AtomicReference<Optional<String>> result = new AtomicReference<>();
        Thread caller = new Thread(() -> result.set(bulkhead.tryCall(() -> {
            ran.set(true);
            return "x";
        })));
        caller.setDaemon(true);
        caller.start();
        boolean parked = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (caller.isAlive() && System.nanoTime() < deadline) {
            Thread.State s = caller.getState();
            if (s == Thread.State.WAITING || s == Thread.State.TIMED_WAITING) {
                parked = true;
                break;
            }
            Thread.onSpinWait();
        }
        caller.join(3_000);
        boolean answered = !caller.isAlive();
        busy.finish();
        caller.join(3_000);
        assertThat(parked).as("the call never waited for a slot, not even briefly").isFalse();
        assertThat(answered).as("the call did not wait for a slot").isTrue();
        assertThat(result.get()).isEmpty();
        assertThat(ran).as("the rejected task did not run").isFalse();
    }

    @Test
    void slotReturnedWhenTaskThrows() {
        Bulkhead bulkhead = new Bulkhead(1);
        IllegalStateException boom = new IllegalStateException("boom");
        assertThatThrownBy(() -> bulkhead.tryCall(() -> {
            throw boom;
        })).isSameAs(boom);
        assertThat(bulkhead.available()).isEqualTo(1);
        StackOverflowError deep = new StackOverflowError("deep");
        assertThatThrownBy(() -> bulkhead.tryCall(() -> {
            throw deep;
        })).isSameAs(deep);
        assertThat(bulkhead.available()).as("an Error gives the slot back too").isEqualTo(1);
        assertThat(bulkhead.tryCall(() -> "ok")).contains("ok");
    }

    @Test
    void rejectionReturnsNoPermit() throws InterruptedException {
        Bulkhead bulkhead = new Bulkhead(1);
        Busy busy = new Busy(bulkhead);
        Optional<String> rejected = bulkhead.tryCall(() -> "x");
        busy.finish();
        assertThat(rejected).isEmpty();
        assertThat(bulkhead.available()).as("still one slot, not two").isEqualTo(1);
    }

    @Test
    void aFreeSlotIsTakenWithoutWaiting() {
        Bulkhead bulkhead = new Bulkhead(1);
        Thread.currentThread().interrupt();
        Optional<String> result;
        try {
            result = bulkhead.tryCall(() -> "ran");
        } finally {
            Thread.interrupted();
        }
        assertThat(result).as("a free slot is taken at once, even by a caller whose interrupt flag is set").contains("ran");
        assertThat(bulkhead.available()).isEqualTo(1);
    }
}
