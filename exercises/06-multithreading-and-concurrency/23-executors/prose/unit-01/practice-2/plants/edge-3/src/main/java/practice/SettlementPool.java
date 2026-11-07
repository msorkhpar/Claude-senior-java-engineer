package practice;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Runs the nightly settlement jobs. Some callers hand it jobs with execute(), others with submit(),
 * and most of them never look at the Future they get back.
 */
public class SettlementPool extends ThreadPoolExecutor {

    /** Where failures go: one call per failed job. */
    public interface Alerts {
        void jobFailed(Throwable cause);
    }

    private final Alerts alerts;

    public SettlementPool(int threads, Alerts alerts) {
        super(threads, threads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), job -> {
            Thread t = new Thread(job);
            t.setDaemon(true);
            return t;
        });
        this.alerts = alerts;
    }

    @Override
    protected void afterExecute(Runnable job, Throwable thrown) {
        super.afterExecute(job, thrown);
        Throwable failure = failureOf(job, thrown);
        if (failure != null) alerts.jobFailed(failure);
    }

    /**
     * What the job that just ran on this thread failed with, or null if it did not fail.
     * Called by afterExecute with its own arguments.
     */
    Throwable failureOf(Runnable job, Throwable thrown) {
        if (thrown != null) return thrown;
        if (job instanceof Future<?> result && result.isDone() && !result.isCancelled()) {
            try {
                result.get();
            } catch (ExecutionException e) {
                return e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
}
