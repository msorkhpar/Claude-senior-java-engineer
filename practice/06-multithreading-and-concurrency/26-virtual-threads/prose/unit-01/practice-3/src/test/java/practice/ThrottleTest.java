package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ThrottleTest {

    /** Polls until the condition holds or 5 s pass; says whether it held. */
    private static boolean eventually(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                return false;
            }
            LockSupport.parkNanos(1_000_000);
        }
        return true;
    }

    @Test
    void returnsResultsInTaskOrder() throws Exception {
        List<Callable<Integer>> tasks = List.of(() -> 1001, () -> 1002, () -> 1003);
        assertThat(Throttle.runAll(tasks, new Semaphore(2))).containsExactly(1001, 1002, 1003);
    }

    /**
     * Eight tasks each enter and then block on a gate. The test waits until three are inside and five
     * are queued on the semaphore, then opens the gate.
     */
    @Test
    void atMostPermitsRunAtOnce() throws Exception {
        Semaphore permits = new Semaphore(3);
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger most = new AtomicInteger();
        CountDownLatch gate = new CountDownLatch(1);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            int n = i;
            tasks.add(() -> {
                most.accumulateAndGet(inside.incrementAndGet(), Math::max);
                try {
                    gate.await(8, TimeUnit.SECONDS);
                } finally {
                    inside.decrementAndGet();
                }
                return n;
            });
        }
        AtomicReference<List<Integer>> results = new AtomicReference<>();
        Thread caller = Thread.ofPlatform().daemon(true).start(() -> {
            try {
                results.set(Throttle.runAll(tasks, permits));
            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        });
        try {
            boolean settled = eventually(() -> inside.get() == 3 && permits.getQueueLength() == 5);
            assertThat(settled).as("3 tasks running and 5 waiting for a permit (running: %s, waiting: %s)",
                    inside.get(), permits.getQueueLength()).isTrue();
        } finally {
            gate.countDown();
        }
        caller.join(5_000);
        assertThat(most.get()).isLessThanOrEqualTo(3);
        assertThat(results.get()).containsExactly(0, 1, 2, 3, 4, 5, 6, 7);
    }

    @Test
    void aFailingTaskGivesBackItsPermit() {
        Semaphore permits = new Semaphore(2);
        List<Callable<Integer>> tasks = List.of(() -> {
            throw new IllegalStateException("no connection");
        });
        assertThatThrownBy(() -> Throttle.runAll(tasks, permits))
                .isInstanceOf(ExecutionException.class)
                .cause().isInstanceOf(IllegalStateException.class);
        assertThat(permits.availablePermits()).as("permits after the failed task").isEqualTo(2);
    }

    @Test
    void aPermitNeverGrantedIsNotReturned() throws Exception {
        Semaphore permits = new Semaphore(2) {
            private final AtomicInteger calls = new AtomicInteger();

            @Override
            public void acquire() throws InterruptedException {
                if (calls.incrementAndGet() == 1) {
                    throw new InterruptedException("interrupted before the permit was granted");
                }
                super.acquire();
            }
        };
        List<Callable<Integer>> tasks = List.of(() -> 1);
        assertThatThrownBy(() -> Throttle.runAll(tasks, permits)).isInstanceOf(ExecutionException.class);
        assertThat(permits.availablePermits()).as("a task that never got its permit gives nothing back").isEqualTo(2);
    }

    @Test
    void waitingForAPermitHasNoTimeout() throws Exception {
        Semaphore permits = new Semaphore(1);
        List<Callable<Integer>> tasks = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int n = i;
            tasks.add(() -> {
                Thread.sleep(1_800);
                return n;
            });
        }
        assertThat(Throttle.runAll(tasks, permits))
                .as("the last task waits over 5 s for the one permit, and still runs").containsExactly(0, 1, 2, 3);
    }
}
