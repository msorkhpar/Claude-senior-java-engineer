package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Gather {
    private Gather() {}
    public static <T> List<T> all(ExecutorService executor, List<Callable<T>> tasks, long timeout, TimeUnit unit, T fallback) throws InterruptedException {
        List<Future<T>> fs = new ArrayList<>();
        for (Callable<T> t : tasks) fs.add(executor.submit(t));
        List<T> out = new ArrayList<>();
        for (Future<T> f : fs) {
            try { out.add(f.get(timeout, unit)); } // a fresh timeout per task, not one deadline
            catch (TimeoutException e) { f.cancel(true); out.add(fallback); }
            catch (ExecutionException | CancellationException e) { out.add(fallback); }
        }
        return out;
    }
}
