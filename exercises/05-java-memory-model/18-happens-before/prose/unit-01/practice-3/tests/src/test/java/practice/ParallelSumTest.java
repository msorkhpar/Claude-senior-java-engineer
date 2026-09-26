package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        long[] values = {100, 20, 3};
        List<CountDownLatch> release = new ArrayList<>();
        Map<Integer, Thread> runners = new ConcurrentHashMap<>();
        List<LongSupplier> parts = new ArrayList<>();
        for (int k = 0; k < values.length; k++) {
            int part = k;
            release.add(new CountDownLatch(1));
            // Every part is slow: it answers only once the caller waits in join() on that part's own thread,
            // and a part nobody waits for gives up after 8 s with 0.
            parts.add(() -> {
                runners.put(part, Thread.currentThread());
                try {
                    return release.get(part).await(8, TimeUnit.SECONDS) ? values[part] : 0;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return 0;
                }
            });
        }
        Thread releaser = new Thread(() -> {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(9);
            while (System.nanoTime() < deadline) {
                ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(caller.threadId());
                if (info != null && info.getThreadState() == Thread.State.WAITING
                        && info.getLockInfo() != null
                        && Thread.class.getName().equals(info.getLockInfo().getClassName())) {
                    int joined = info.getLockInfo().getIdentityHashCode();
                    runners.forEach((part, runner) -> {
                        if (System.identityHashCode(runner) == joined) {
                            release.get(part).countDown();
                        }
                    });
                }
                Thread.onSpinWait();
            }
        });
        releaser.setDaemon(true);
        releaser.start();
        try {
            assertThat(ParallelSum.sum(parts)).isEqualTo(123);
        } finally {
            release.forEach(CountDownLatch::countDown);
        }
    }
}
