package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Fallback {
    private Fallback() {}
    public static <T> T within(ExecutorService executor, Callable<T> task, long timeout, TimeUnit unit, T fallback) {
        Future<T> f = executor.submit(task);
        try { return f.get(timeout, TimeUnit.MILLISECONDS); } // unit ignored
        catch (TimeoutException e) { f.cancel(true); return fallback; }
        catch (ExecutionException e) { throw new RuntimeException(e.getCause()); }
        catch (InterruptedException e) { f.cancel(true); Thread.currentThread().interrupt(); return fallback; }
    }
}
