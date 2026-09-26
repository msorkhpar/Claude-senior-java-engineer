package practice;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Squares {

    private Squares() {
    }

    /** Single responsibility: producing tasks. */
    public static final class TaskProducer {
        public List<Callable<Long>> createTasks(int count) {
            throw new UnsupportedOperationException("write createTasks");
        }
    }

    /** Single responsibility: running tasks on the pool it is given. */
    public static final class TaskExecutor {

        public TaskExecutor(ExecutorService executorService) {
            throw new UnsupportedOperationException("write the constructor");
        }

        public List<Future<Long>> submitAll(List<Callable<Long>> tasks) {
            throw new UnsupportedOperationException("write submitAll");
        }

        public void shutdown() {
            throw new UnsupportedOperationException("write shutdown");
        }
    }

    /** Single responsibility: aggregating results. */
    public static final class ResultAggregator {
        public long sumResults(List<Future<Long>> futures) throws ExecutionException, InterruptedException {
            throw new UnsupportedOperationException("write sumResults");
        }
    }
}
