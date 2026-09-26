package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
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
        throw new UnsupportedOperationException("write executeAll");
    }

    /** The square of each number, computed on threadCount threads. */
    public static List<Integer> squares(List<Integer> numbers, int threadCount) throws InterruptedException {
        throw new UnsupportedOperationException("write squares");
    }

    /** The cube of each number, computed on threadCount threads. */
    public static List<Integer> cubes(List<Integer> numbers, int threadCount) throws InterruptedException {
        throw new UnsupportedOperationException("write cubes");
    }
}
