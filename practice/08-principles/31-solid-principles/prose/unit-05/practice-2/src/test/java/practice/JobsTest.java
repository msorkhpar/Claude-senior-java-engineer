package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import static org.assertj.core.api.Assertions.*;

class JobsTest {

    /** Runs each job at once on the calling thread: deterministic, no pool. */
    static final class DirectStrategy implements Jobs.ExecutionStrategy {
        int executed;
        boolean shut;

        @Override public <T> Future<T> execute(Callable<T> task) {
            executed++;
            FutureTask<T> future = new FutureTask<>(task);
            future.run();
            return future;
        }

        @Override public void shutdown() { shut = true; }
    }

    /** Keeps each task it is handed and runs none of them. */
    static final class StoringStrategy implements Jobs.ExecutionStrategy {
        final List<Callable<?>> stored = new ArrayList<>();

        @Override public <T> Future<T> execute(Callable<T> task) {
            stored.add(task);
            return new FutureTask<>(task);
        }

        @Override public void shutdown() { }
    }

    @Test
    void jobsRunOnVirtualThreads() throws Exception {
        var scheduler = new Jobs.JobScheduler(new Jobs.VirtualThreadStrategy());
        try {
            assertThat(scheduler.scheduleJob(() -> Thread.currentThread().isVirtual()).get()).isTrue();
            assertThat(scheduler.scheduleJob(() -> 6 * 7).get()).isEqualTo(42);
            assertThat(scheduler.getJobCount()).isEqualTo(2);
        } finally {
            scheduler.shutdown();
        }
        var strategy = new Jobs.VirtualThreadStrategy();
        assertThat(strategy.execute(() -> Thread.currentThread().isVirtual()).get()).isTrue();
        strategy.shutdown();
        assertThatThrownBy(() -> strategy.execute(() -> 1)).isInstanceOf(RejectedExecutionException.class);
    }

    @Test
    void anInjectedStrategyRunsEveryJob() throws Exception {
        var direct = new DirectStrategy();
        var scheduler = new Jobs.JobScheduler(direct);
        Thread caller = Thread.currentThread();
        assertThat(scheduler.scheduleJob(Thread::currentThread).get()).isSameAs(caller);
        assertThat(scheduler.scheduleJob(() -> "done").get()).isEqualTo("done");
        assertThat(direct.executed).isEqualTo(2);
        var storing = new StoringStrategy();
        var later = new Jobs.JobScheduler(storing);
        Callable<String> job = () -> "not yet";
        later.scheduleJob(job);
        assertThat(later.getJobCount()).isEqualTo(1);
        assertThat(storing.stored).containsExactly(job);
        assertThat(storing.stored.get(0)).isSameAs(job);
    }

    @Test
    void shutdownReachesTheStrategy() throws Exception {
        var direct = new DirectStrategy();
        var scheduler = new Jobs.JobScheduler(direct);
        scheduler.scheduleJob(() -> 1).get();
        scheduler.shutdown();
        assertThat(direct.shut).isTrue();
    }

    @Test
    void aNullStrategyIsRejected() throws Exception {
        new Jobs.JobScheduler(new DirectStrategy()).getJobCount();
        assertThatNullPointerException().isThrownBy(() -> new Jobs.JobScheduler(null));
    }

    @Test
    void aJobIsCountedBeforeItRuns() throws Exception {
        var direct = new DirectStrategy();
        var scheduler = new Jobs.JobScheduler(direct);
        assertThat(scheduler.scheduleJob(scheduler::getJobCount).get()).as("counted before the strategy ran it").isEqualTo(1);
        assertThat(scheduler.scheduleJob(scheduler::getJobCount).get()).isEqualTo(2);
    }
}
