package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Outcomes {
    private Outcomes() {}
    public static String describe(Future<?> future, long timeout, TimeUnit unit) throws InterruptedException {
        try { return "value: " + future.get(timeout, TimeUnit.MILLISECONDS); } // unit ignored
        catch (ExecutionException e) { return "failed: " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage(); }
        catch (CancellationException e) { return "cancelled"; }
        catch (TimeoutException e) { return "timed out"; }
    }
}
