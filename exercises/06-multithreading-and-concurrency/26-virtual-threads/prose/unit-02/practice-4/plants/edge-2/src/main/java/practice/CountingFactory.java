package practice;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class CountingFactory implements ThreadFactory {

    private final ThreadFactory virtualThreads;
    private final AtomicInteger running = new AtomicInteger();

    public CountingFactory(String prefix) {
        this.virtualThreads = Thread.ofVirtual().name(prefix, 0).factory();
    }

    /** An unstarted virtual thread named prefix + n that runs the task and is counted while it does. */
    @Override
    public Thread newThread(Runnable task) {
        return virtualThreads.newThread(() -> {
            running.incrementAndGet();
            task.run();
            running.decrementAndGet();
        });
    }

    /** How many of this factory's threads are running their task. */
    public int running() {
        return running.get();
    }
}
