package practice;

import java.util.concurrent.ThreadFactory;

/**
 * Creates the threads of one component's pool, for example
 * {@code Executors.newFixedThreadPool(4, new ServiceThreads("audit", false))}.
 * A background component's work may be dropped at exit; a foreground component's must finish.
 */
public class ServiceThreads implements ThreadFactory {

    private final String component;
    private final boolean background;

    public ServiceThreads(String component, boolean background) {
        this.component = component;
        this.background = background;
    }

    @Override
    public Thread newThread(Runnable task) {
        // TODO: Create the (unstarted) thread that will run this pool's tasks. In a thread dump,
        //       on-call must be able to tell which component a thread belongs to and which of
        //       its threads it is, and whether the JVM waits for it at exit must depend only on
        //       what the component declared.
        throw new IllegalStateException("TODO: newThread");
    }
}
