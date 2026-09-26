package practice;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class SquaresTest {

    /** Runs each task at once on the calling thread, and counts what it was handed. */
    static final class InlineExecutor extends AbstractExecutorService {
        int submitted;
        boolean shut;

        @Override public void execute(Runnable command) { submitted++; command.run(); }
        @Override public void shutdown() { shut = true; }
        @Override public List<Runnable> shutdownNow() { shut = true; return List.of(); }
        @Override public boolean isShutdown() { return shut; }
        @Override public boolean isTerminated() { return shut; }
        @Override public boolean awaitTermination(long timeout, TimeUnit unit) { return shut; }
    }

    @Test
    void sumsTheSquaresOnAThreadPool() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            var executor = new Squares.TaskExecutor(pool);
            var futures = executor.submitAll(new Squares.TaskProducer().createTasks(5));
            assertThat(futures).hasSize(5);
            for (int i = 0; i < 5; i++) {
                assertThat(futures.get(i).get()).isEqualTo((long) i * i);
            }
            assertThat(new Squares.ResultAggregator().sumResults(futures)).isEqualTo(30L);
            var many = executor.submitAll(new Squares.TaskProducer().createTasks(100_000));
            assertThat(new Squares.ResultAggregator().sumResults(many)).isEqualTo(333_328_333_350_000L);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void tasksRunOnTheInjectedExecutor() throws Exception {
        var inline = new InlineExecutor();
        var futures = new Squares.TaskExecutor(inline).submitAll(new Squares.TaskProducer().createTasks(4));
        assertThat(inline.submitted).isEqualTo(4);
        assertThat(new Squares.ResultAggregator().sumResults(futures)).isEqualTo(14L);
        var many = new Squares.TaskExecutor(inline).submitAll(new Squares.TaskProducer().createTasks(50));
        assertThat(inline.submitted).isEqualTo(54);
        assertThat(new Squares.ResultAggregator().sumResults(many)).isEqualTo(40_425L);
    }

    @Test
    void shutdownStopsTheInjectedExecutor() throws Exception {
        var inline = new InlineExecutor();
        var executor = new Squares.TaskExecutor(inline);
        executor.submitAll(new Squares.TaskProducer().createTasks(2));
        executor.shutdown();
        assertThat(inline.shut).isTrue();
    }

    @Test
    void aNegativeCountIsRejectedAndZeroIsEmpty() throws Exception {
        var producer = new Squares.TaskProducer();
        assertThat(producer.createTasks(0)).isEmpty();
        assertThatIllegalArgumentException().isThrownBy(() -> producer.createTasks(-1));
    }

    @Test
    void shutdownLetsQueuedTasksFinish() throws Exception {
        java.util.concurrent.ExecutorService single = java.util.concurrent.Executors.newSingleThreadExecutor();
        Squares.TaskExecutor executor = new Squares.TaskExecutor(single);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        java.util.List<java.util.concurrent.Callable<Long>> tasks = java.util.List.of(
                () -> release.await(5, java.util.concurrent.TimeUnit.SECONDS) ? 1L : -1L,
                () -> 2L);
        java.util.List<java.util.concurrent.Future<Long>> futures = executor.submitAll(tasks);
        executor.shutdown();
        release.countDown();
        assertThat(futures.get(0).get(5, java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(1L);
        assertThat(futures.get(1).get(5, java.util.concurrent.TimeUnit.SECONDS)).as("the queued task still ran").isEqualTo(2L);
    }
}
