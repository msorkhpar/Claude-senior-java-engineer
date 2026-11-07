package practice;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Holds traffic back until every cache loader of an instance has finished. Each loader runs on
 * a thread of its own and calls runLoader; the startup thread calls awaitLoaded before it tells
 * the load balancer the instance is ready. A loader that fails doesn't stop the instance from
 * starting: it starts without that cache, and failed() names the caches it is missing.
 */
public class StartupGate {

    private final Queue<String> failed = new ConcurrentLinkedQueue<>();
    private final CountDownLatch remaining;

    public StartupGate(int loaders) {
        this.remaining = new CountDownLatch(loaders);
    }

    /** Runs one cache's loader on the calling thread. Never throws what the loader throws. */
    public void runLoader(String cache, Runnable loader) {
        try {
            loader.run();
        } catch (RuntimeException e) {
            failed.add(cache);
        } finally {
            remaining.countDown();
        }
    }

    /** Waits at most the given time for every loader to finish. True if they all have. */
    public boolean awaitLoaded(long timeout, TimeUnit unit) throws InterruptedException {
        return remaining.await(timeout, unit);
    }

    /** The caches whose loader failed, for the startup log. */
    public List<String> failed() {
        return List.copyOf(failed);
    }
}
