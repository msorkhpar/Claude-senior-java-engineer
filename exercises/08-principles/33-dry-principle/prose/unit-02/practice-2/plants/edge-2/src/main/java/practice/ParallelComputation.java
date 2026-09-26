package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        List<Future<T>> futures = executor.invokeAll(tasks);
        List<T> results = new ArrayList<>();
        for (Future<T> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                throw new RuntimeException("Task execution failed", e);
            }
        }
        return results;
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
