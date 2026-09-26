package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Replicas {
    private Replicas() {}
    public static <T> T first(ExecutorService executor, List<Callable<T>> replicas) throws InterruptedException, ExecutionException {
        return executor.invokeAny(replicas);
    }
    public static <T> List<T> inArrivalOrder(ExecutorService executor, List<Callable<T>> tasks) throws InterruptedException, ExecutionException {
        CompletionService<T> cs = new ExecutorCompletionService<>(executor);
        for (Callable<T> t : tasks) cs.submit(t);
        List<T> out = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            Future<T> f = cs.poll(1, TimeUnit.SECONDS); // a task slower than 1 s is dropped
            if (f != null) out.add(f.get());
        }
        return out;
    }
}
