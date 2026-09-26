package practice;
import java.util.*; import java.util.concurrent.*; import java.util.concurrent.atomic.*; import java.util.function.*;
public final class Outcomes {
    private Outcomes() {}
    public static String describe(Future<?> future, long timeout, TimeUnit unit) throws InterruptedException {
        try { return "value: " + future.get(timeout, unit); }
        catch (ExecutionException e) {
            Throwable c = e.getCause();
            while (c.getCause() != null) c = c.getCause(); // digs to the root cause, past what the task threw
            return "failed: " + c.getClass().getSimpleName() + ": " + c.getMessage(); }
        catch (CancellationException e) { return "cancelled"; }
        catch (TimeoutException e) { return "timed out"; }
    }
}
