package practice;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
public final class ParallelComputation {
    private ParallelComputation() {}
    public static <T> List<T> executeAll(List<Callable<T>> tasks, int threadCount) throws InterruptedException { if (threadCount <= 0) throw new IllegalArgumentException("threadCount"); ExecutorService ex = Executors.newFixedThreadPool(threadCount); try { List<Future<T>> fs = ex.invokeAll(tasks); List<T> out = new ArrayList<>(); for (Future<T> f : fs) { try { out.add(f.get()); } catch (ExecutionException e) { Throwable c = e.getCause(); if (c instanceof RuntimeException r) throw r; throw new RuntimeException(c); } } return out; } finally { ex.shutdown(); ex.awaitTermination(5, TimeUnit.SECONDS); } }
    public static List<Integer> squares(List<Integer> numbers, int threadCount) throws InterruptedException { List<Callable<Integer>> t = new ArrayList<>(); for (Integer n : numbers) t.add(() -> n * n); return executeAll(t, threadCount); }
    public static List<Integer> cubes(List<Integer> numbers, int threadCount) throws InterruptedException { List<Callable<Integer>> t = new ArrayList<>(); for (Integer n : numbers) t.add(() -> n * n * n); return executeAll(t, threadCount); }
}
