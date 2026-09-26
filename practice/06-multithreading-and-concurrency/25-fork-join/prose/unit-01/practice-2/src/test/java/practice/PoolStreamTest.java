package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PoolStreamTest {

    @Test
    void sumsTheMappedValues() {
        assertThat(PoolStream.sum(new long[]{1, 2, 3}, 3, x -> x * x)).isEqualTo(14L);
    }

    @Test
    void runsOnTheCustomPoolsWorkers() {
        List<Thread> threads = Collections.synchronizedList(new ArrayList<>());
        long total = PoolStream.sum(new long[]{1, 2, 3, 4}, 3, x -> {
            threads.add(Thread.currentThread());
            return x;
        });
        assertThat(total).isEqualTo(10L);
        assertThat(threads).hasSize(4).allSatisfy(t -> {
            assertThat(t).as("a pool worker, not the caller").isInstanceOf(ForkJoinWorkerThread.class);
            ForkJoinPool pool = ((ForkJoinWorkerThread) t).getPool();
            assertThat(pool).isNotSameAs(ForkJoinPool.commonPool());
            assertThat(pool.getParallelism()).isEqualTo(3);
        });
    }

    @Test
    void theStreamRunsInParallel() {
        CyclicBarrier both = new CyclicBarrier(2);
        long total = PoolStream.sum(new long[]{5, 7}, 3, x -> {
            try {
                both.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("value " + x + " was mapped alone: the stream is not parallel", e);
            }
            return x;
        });
        assertThat(total).isEqualTo(12L);
    }

    @Test
    void thePoolIsShutDownAfterward() {
        List<ForkJoinPool> pools = Collections.synchronizedList(new ArrayList<>());
        PoolStream.sum(new long[]{1, 2}, 3, x -> {
            if (Thread.currentThread() instanceof ForkJoinWorkerThread w) {
                pools.add(w.getPool());
            }
            return x;
        });
        assertThat(pools).isNotEmpty().allMatch(ForkJoinPool::isShutdown, "is shut down");
    }

    @Test
    void thePoolIsShutDownWhenFFails() {
        List<ForkJoinPool> pools = Collections.synchronizedList(new ArrayList<>());
        IllegalStateException boom = new IllegalStateException("bad value");
        Throwable thrown = null;
        try {
            PoolStream.sum(new long[]{1, 2, 3, 4}, 3, x -> {
                if (Thread.currentThread() instanceof ForkJoinWorkerThread w) {
                    pools.add(w.getPool());
                }
                if (x == 3) {
                    throw boom;
                }
                return x;
            });
        } catch (Throwable t) {
            thrown = t;
        }
        assertThat(thrown).as("f's failure reaches the caller").isInstanceOf(IllegalStateException.class);
        assertThat(pools).isNotEmpty().allMatch(ForkJoinPool::isShutdown, "is shut down");
    }
}
