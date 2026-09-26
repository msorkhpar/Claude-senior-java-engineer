package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Retry {
    private Retry() {}
    public static <T> T call(ExecutorService executor, Callable<T> task, int maxRetries) throws Exception {
        Exception last = new IllegalStateException("no attempt succeeded");
        for (int a = 0; a <= maxRetries; a++) {
            Future<T> f = executor.submit(task);
            try { return f.get(5, TimeUnit.SECONDS); }
            catch (ExecutionException e) { last = e.getCause() instanceof Exception c ? c : e; }
            catch (TimeoutException e) { f.cancel(true); } // a timed-out attempt does not become the last failure
        }
        throw last;
    }
}
