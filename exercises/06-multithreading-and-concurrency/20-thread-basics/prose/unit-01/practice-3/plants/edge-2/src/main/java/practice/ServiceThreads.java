package practice;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates the threads of one component's pool, for example
 * {@code Executors.newFixedThreadPool(4, new ServiceThreads("audit", false))}.
 * A background component's work may be dropped at exit; a foreground component's must finish.
 */
public class ServiceThreads implements ThreadFactory {

    private final String component;
    private final boolean background;
    private final AtomicInteger created = new AtomicInteger();

    public ServiceThreads(String component, boolean background) {
        this.component = component;
        this.background = background;
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new Thread(task, component + "-" + created.incrementAndGet());
        if (background) thread.setDaemon(true);
        return thread;
    }
}
