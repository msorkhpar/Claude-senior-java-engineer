package practice;
import java.util.*; import java.util.concurrent.*;
public final class FanOut {
    private FanOut() {}
    public static <T> List<T> all(List<Callable<T>> tasks) throws Exception {
        ExecutorService ex = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<T>> fs = new ArrayList<>();
        for (Callable<T> t : tasks) fs.add(ex.submit(t));
        List<T> out = new ArrayList<>();
        for (Future<T> f : fs) {
            try { out.add(f.get()); } catch (ExecutionException e) { throw (Exception) e.getCause(); }
        }
        return out; // executor never closed; on a failure the later tasks are left running
    }
}
