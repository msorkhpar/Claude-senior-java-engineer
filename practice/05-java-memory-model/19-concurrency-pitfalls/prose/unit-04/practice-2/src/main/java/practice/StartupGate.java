package practice;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Holds traffic back until every cache loader of an instance has finished. Each loader runs on
 * a thread of its own and calls runLoader; the startup thread calls awaitLoaded before it tells
 * the load balancer the instance is ready. A loader that fails doesn't stop the instance from
 * starting: it starts without that cache, and failed() names the caches it is missing.
 */
public class StartupGate {

    private final Queue<String> failed = new ConcurrentLinkedQueue<>();
    private final int loaders;

    public StartupGate(int loaders) {
        this.loaders = loaders;
    }

    /** Runs one cache's loader on the calling thread. Never throws what the loader throws. */
    public void runLoader(String cache, Runnable loader) {
        // TODO: Run the loader and record that this cache is done, whether it loaded or failed.
        //       A failed cache goes into `failed`.
        throw new IllegalStateException("TODO: runLoader");
    }

    /** Waits at most the given time for every loader to finish. True if they all have. */
    public boolean awaitLoaded(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO: Answer true as soon as all `loaders` loaders have finished, even if they finished
        //       before this call; false once the time is up.
        throw new IllegalStateException("TODO: awaitLoaded");
    }

    /** The caches whose loader failed, for the startup log. */
    public List<String> failed() {
        return List.copyOf(failed);
    }
}
