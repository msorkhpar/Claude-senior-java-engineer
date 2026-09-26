package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PoolRunnerTest {

    private static final List<Integer> ITEMS = IntStream.range(0, 2_000).boxed().toList();

    @Test
    void mapsEveryItemInOrder() {
        assertThat(PoolRunner.mapInPool(List.of(1, 2, 3, 4), 2, x -> x * 10)).containsExactly(10, 20, 30, 40);
        assertThat(PoolRunner.mapInPool(List.of("a", "b"), 4, String::toUpperCase)).containsExactly("A", "B");
        assertThat(PoolRunner.mapInPool(ITEMS, 3, x -> x + 1))
                .isEqualTo(IntStream.range(1, 2_001).boxed().toList());
        assertThat(PoolRunner.mapInPool(List.<Integer>of(), 2, x -> x)).isEmpty();
    }

    @Test
    void runsInsideTheNewPool() {
        Set<Thread> threads = ConcurrentHashMap.newKeySet();
        PoolRunner.mapInPool(ITEMS, 2, x -> {
            threads.add(Thread.currentThread());
            return x;
        });
        assertThat(threads).isNotEmpty().doesNotContain(Thread.currentThread());
        assertThat(threads).allSatisfy(thread -> {
            assertThat(thread).isInstanceOf(ForkJoinWorkerThread.class);
            assertThat(((ForkJoinWorkerThread) thread).getPool()).isNotSameAs(ForkJoinPool.commonPool());
        });
    }

    @Test
    void poolIsShutDownAfterwards() {
        Set<ForkJoinPool> pools = ConcurrentHashMap.newKeySet();
        PoolRunner.mapInPool(ITEMS, 2, x -> {
            if (Thread.currentThread() instanceof ForkJoinWorkerThread worker) {
                pools.add(worker.getPool());
            }
            return x;
        });
        pools.remove(ForkJoinPool.commonPool());
        assertThat(pools).isNotEmpty().allSatisfy(pool -> assertThat(pool.isShutdown()).isTrue());
    }

    @Test
    void failureKeepsItsType() {
        assertThatThrownBy(() -> PoolRunner.mapInPool(ITEMS, 2, x -> {
            if (x == 1_234) {
                throw new IllegalArgumentException("bad item " + x);
            }
            return x;
        })).isInstanceOf(IllegalArgumentException.class).hasStackTraceContaining("bad item 1234");
    }
}
