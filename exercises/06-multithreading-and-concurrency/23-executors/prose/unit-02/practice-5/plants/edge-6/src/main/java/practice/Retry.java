package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Retry {
    private Retry() {}
    public static <T> T call(ExecutorService executor, Callable<T> task, int maxRetries) throws Exception {
        Exception last = null;
        for (int a = 0; a <= maxRetries; a++) {
            Future<T> f = executor.submit(task);
            try { return f.get(500, TimeUnit.MILLISECONDS); } // gives each attempt 0.5 s, not 5 s
            catch (ExecutionException e) { last = e.getCause() instanceof Exception c ? c : e; }
            catch (TimeoutException e) { f.cancel(true); last = e; }
        }
        throw last;
    }
}
