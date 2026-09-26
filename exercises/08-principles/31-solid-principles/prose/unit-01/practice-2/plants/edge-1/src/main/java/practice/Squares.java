package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class Squares {

    private Squares() {
    }

    /** Single responsibility: producing tasks. Stateless, so thread-safe by default. */
    public static final class TaskProducer {
        public List<Callable<Long>> createTasks(int count) {
            if (count < 0) {
                throw new IllegalArgumentException("Count must be non-negative");
            }
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                final long taskId = i;
                tasks.add(() -> taskId * taskId);
            }
            return tasks;
        }
    }

    /** Single responsibility: running tasks on the pool it is given. */
    public static final class TaskExecutor {
        private final ExecutorService executorService = java.util.concurrent.Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        public TaskExecutor(ExecutorService executorService) {
            Objects.requireNonNull(executorService);
        }

        public List<Future<Long>> submitAll(List<Callable<Long>> tasks) {
            List<Future<Long>> futures = new ArrayList<>();
            for (Callable<Long> task : tasks) {
                futures.add(executorService.submit(task));
            }
            return futures;
        }

        public void shutdown() {
            executorService.shutdown();
        }
    }

    /** Single responsibility: aggregating results. */
    public static final class ResultAggregator {
        public long sumResults(List<Future<Long>> futures) throws ExecutionException, InterruptedException {
            long sum = 0;
            for (Future<Long> future : futures) {
                sum += future.get();
            }
            return sum;
        }
    }
}
