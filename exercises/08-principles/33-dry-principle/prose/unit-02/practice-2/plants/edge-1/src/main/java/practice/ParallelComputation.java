package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** The executor lifecycle, written once, and two computations that reuse it. */
public final class ParallelComputation {

    private ParallelComputation() {
    }

    /** Runs every task on a pool of threadCount threads and returns their results in task order. */
    public static <T> List<T> executeAll(List<Callable<T>> tasks, int threadCount) throws InterruptedException {
        Objects.requireNonNull(tasks, "tasks must not be null");
        if (threadCount <= 0) {
            throw new IllegalArgumentException("threadCount must be positive");
        }
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            CompletionService<T> done = new ExecutorCompletionService<>(executor);
            for (Callable<T> task : tasks) {
                done.submit(task);
            }
            List<T> results = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                try {
                    results.add(done.take().get());
                } catch (ExecutionException e) {
                    throw new RuntimeException("Task execution failed", e);
                }
            }
            return results;
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    /** The square of each number, computed on threadCount threads. */
    public static List<Integer> squares(List<Integer> numbers, int threadCount) throws InterruptedException {
        return executeAll(numbers.stream().<Callable<Integer>>map(n -> () -> n * n).toList(), threadCount);
    }

    /** The cube of each number, computed on threadCount threads. */
    public static List<Integer> cubes(List<Integer> numbers, int threadCount) throws InterruptedException {
        return executeAll(numbers.stream().<Callable<Integer>>map(n -> () -> n * n * n).toList(), threadCount);
    }
}
