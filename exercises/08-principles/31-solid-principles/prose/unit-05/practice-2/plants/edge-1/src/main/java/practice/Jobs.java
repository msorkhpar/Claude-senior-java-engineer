package practice;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public final class Jobs {

    private Jobs() {
    }

    /** The abstraction the scheduler depends on. */
    public interface ExecutionStrategy {
        <T> Future<T> execute(Callable<T> task);

        void shutdown();
    }

    /** One detail: a virtual thread per task. */
    public static final class VirtualThreadStrategy implements ExecutionStrategy {
        private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        public <T> Future<T> execute(Callable<T> task) {
            return executor.submit(task);
        }

        public void shutdown() {
            executor.shutdown();
        }
    }

    /** The high-level module: knows nothing about thread models. */
    public static final class JobScheduler {
        private final ExecutionStrategy strategy;
        private final AtomicInteger jobs = new AtomicInteger();
        private final ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor();

        public JobScheduler(ExecutionStrategy strategy) {
            this.strategy = Objects.requireNonNull(strategy);
        }

        public <T> Future<T> scheduleJob(Callable<T> job) {
            jobs.incrementAndGet();
            return threads.submit(job);
        }

        public int getJobCount() {
            return jobs.get();
        }

        public void shutdown() {
            strategy.shutdown();
        }
    }
}
