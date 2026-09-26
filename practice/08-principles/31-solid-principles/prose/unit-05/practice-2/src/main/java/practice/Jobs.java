package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

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

        public <T> Future<T> execute(Callable<T> task) {
            throw new UnsupportedOperationException("write execute");
        }

        public void shutdown() {
            throw new UnsupportedOperationException("write shutdown");
        }
    }

    /** The high-level module: knows nothing about thread models. */
    public static final class JobScheduler {

        public JobScheduler(ExecutionStrategy strategy) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public <T> Future<T> scheduleJob(Callable<T> job) {
            throw new UnsupportedOperationException("write scheduleJob");
        }

        public int getJobCount() {
            throw new UnsupportedOperationException("write getJobCount");
        }

        public void shutdown() {
            throw new UnsupportedOperationException("write shutdown");
        }
    }
}
