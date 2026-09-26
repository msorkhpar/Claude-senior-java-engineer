package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ScoreBoardTest {

    private static Thread daemon(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /** A change that, on its first call only, says it has started and then holds until released (at most 9 s). */
    private static IntUnaryOperator heldOnce(int add, CountDownLatch started, CountDownLatch release) {
        AtomicBoolean first = new AtomicBoolean(true);
        return x -> {
            if (first.getAndSet(false)) {
                started.countDown();
                try {
                    release.await(9, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            return x + add;
        };
    }

    /** Waits, at most 5 s, until {@code thread} has finished or is blocked on a lock {@code holder} holds. */
    private static void awaitDoneOrBlockedBy(Thread thread, Thread holder) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (thread.getState() == Thread.State.TERMINATED) {
                return;
            }
            ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(thread.threadId());
            if (info != null && info.getLockOwnerId() == holder.threadId()) {
                return;
            }
            Thread.sleep(1);
        }
    }

    @Test
    void updatesSlots() {
        ScoreBoard board = new ScoreBoard(3);
        assertThat(board.update(0, x -> x + 5)).isEqualTo(5);
        assertThat(board.update(2, x -> x + 1)).isEqualTo(1);
        assertThat(board.update(0, x -> x * 3)).isEqualTo(15);
        assertThat(board.get(0)).isEqualTo(15);
        assertThat(board.snapshot()).containsExactly(15, 0, 1);
    }

    @Test
    void concurrentUpdatesOfOneSlotAreKept() throws InterruptedException {
        ScoreBoard board = new ScoreBoard(3);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread slow = daemon(() -> board.update(1, heldOnce(10, started, release)));
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the first update started").isTrue();
        Thread quick = daemon(() -> board.update(1, x -> x + 1));
        awaitDoneOrBlockedBy(quick, slow);
        release.countDown();
        slow.join(5_000);
        quick.join(5_000);
        assertThat(slow.isAlive() || quick.isAlive()).isFalse();
        assertThat(board.get(1)).as("both updates of slot 1 are kept").isEqualTo(11);
    }

    @Test
    void slotsDoNotWaitForEachOther() throws InterruptedException {
        ScoreBoard board = new ScoreBoard(3);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Thread slow = daemon(() -> board.update(1, heldOnce(10, started, release)));
        assertThat(started.await(5, TimeUnit.SECONDS)).as("the slot 1 update started").isTrue();
        Thread other = daemon(() -> board.update(0, x -> x + 7));
        other.join(4_000);
        boolean waited = other.isAlive();
        release.countDown();
        slow.join(5_000);
        other.join(5_000);
        assertThat(waited).as("the slot 0 update waited for the slot 1 update").isFalse();
        assertThat(board.snapshot()).containsExactly(7, 10, 0);
    }
}
