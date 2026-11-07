package practice;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/** Stops the invoice service's worker pool when the service is redeployed. */
public class Drain {

    /**
     * Stops the pool within the deploy's grace period and returns the tasks that never started,
     * so the caller can put them back on the message broker. Called by the deploy's shutdown hook.
     */
    public static List<Runnable> stop(ExecutorService pool, long grace, TimeUnit unit) {
        // TODO: The orchestrator gives each deploy a grace period before it kills the process.
        //       Let the work already handed to the pool finish if it can within the grace
        //       period; past it, stop what is still running, and hand back what never started.
        throw new IllegalStateException("TODO: stop");
    }
}
