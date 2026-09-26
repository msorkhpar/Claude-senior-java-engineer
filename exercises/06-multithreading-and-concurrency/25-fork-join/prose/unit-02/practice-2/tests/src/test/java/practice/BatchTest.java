package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BatchTest {

    private ForkJoinPool pool;

    @BeforeEach
    void startPool() {
        pool = new ForkJoinPool(2);
    }

    @AfterEach
    void stopPool() {
        pool.shutdownNow();
    }

    private static RecursiveTask<String> task(Supplier<String> body) {
        return new RecursiveTask<>() {
            @Override
            protected String compute() {
                return body.get();
            }
        };
    }

    /** Built at runtime, so no answer can pass on a shared literal. */
    private static String word(String w) {
        return new String(w.toCharArray());
    }

    @Test
    void returnsEachTasksResult() {
        CountDownLatch alphaDone = new CountDownLatch(1);
        RecursiveTask<String> alpha = task(() -> {
            String w = word("alpha");
            alphaDone.countDown();
            return w;
        });
        RecursiveTask<String> beta = task(() -> {
            try {
                if (!alphaDone.await(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("alpha never finished");
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return word("beta");
        });
        assertThat(Batch.runAll(pool, List.of(alpha, beta))).containsExactly("alpha", "beta");
        assertThat(Batch.runAll(pool, List.<RecursiveTask<String>>of())).isEmpty();
    }

    @Test
    void submitsEveryTaskBeforeJoiningAny() {
        CyclicBarrier bothStarted = new CyclicBarrier(2);
        Supplier<String> meet = () -> {
            try {
                bothStarted.await(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                throw new IllegalStateException("the other task was not running at the same time", e);
            }
            return "";
        };
        List<String> results = Batch.runAll(pool, List.of(
                task(() -> meet.get() + word("left")), task(() -> meet.get() + word("right"))));
        assertThat(results).containsExactly("left", "right");
    }

    @Test
    void runsEveryTaskInTheGivenPool() {
        List<ForkJoinPool> seen = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        Supplier<String> where = () -> {
            seen.add(ForkJoinTask.getPool());
            return "";
        };
        Batch.runAll(pool, List.of(task(where), task(where), task(where)));
        assertThat(seen).hasSize(3).allMatch(p -> p == pool, "is the pool passed in");
    }
}
