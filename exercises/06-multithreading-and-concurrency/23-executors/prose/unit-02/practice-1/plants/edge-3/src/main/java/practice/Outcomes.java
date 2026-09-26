package practice;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class Outcomes {

    private Outcomes() {
    }

    /** Waits at most timeout for the future and describes how its task ended. */
    public static String describe(Future<?> future, long timeout, TimeUnit unit) throws InterruptedException {
        if (!future.isDone()) {
            return "timed out";
        }
        try {
            return "value: " + future.get(timeout, unit);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return "failed: " + cause.getClass().getSimpleName() + ": " + cause.getMessage();
        } catch (CancellationException e) {
            return "cancelled";
        } catch (TimeoutException e) {
            return "timed out";
        }
    }
}
