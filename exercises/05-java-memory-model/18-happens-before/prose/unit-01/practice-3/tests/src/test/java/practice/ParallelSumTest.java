package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ParallelSumTest {

    @Test
    void sumsEveryPart() throws Exception {
        assertThat(ParallelSum.sum(List.of(() -> 1, () -> 2, () -> 3))).isEqualTo(6);
        assertThat(ParallelSum.sum(List.of(() -> 40, () -> 2))).isEqualTo(42);
        assertThat(ParallelSum.sum(List.of())).isZero();
    }

    @Test
    void runsEachPartOnItsOwnThread() throws Exception {
        Thread caller = Thread.currentThread();
        Set<Thread> ran = ConcurrentHashMap.newKeySet();
        LongSupplier part = () -> {
            ran.add(Thread.currentThread());
            return 5;
        };
        assertThat(ParallelSum.sum(List.of(part, part, part))).isEqualTo(15);
        assertThat(ran).hasSize(3).doesNotContain(caller);
    }

    @Test
    void waitsForASlowPart() throws Exception {
        Thread caller = Thread.currentThread();
        CountDownLatch release = new CountDownLatch(1);
        LongSupplier slow = () -> {
            try {
                release.await(8, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return 100;
        };
        // Releases the slow part only once the caller is waiting in join() on a worker thread.
        Thread releaser = new Thread(() -> {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(caller.threadId());
                if (info != null && info.getThreadState() == Thread.State.WAITING
                        && info.getLockInfo() != null
                        && Thread.class.getName().equals(info.getLockInfo().getClassName())) {
                    release.countDown();
                    return;
                }
                Thread.onSpinWait();
            }
        });
        releaser.setDaemon(true);
        releaser.start();
        try {
            assertThat(ParallelSum.sum(List.of(slow, () -> 1, () -> 2))).isEqualTo(103);
        } finally {
            release.countDown();
        }
    }
}
