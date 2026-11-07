package practice;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Makes the threads that handle requests, and reports how many of them are busy for the
 * dashboard's "busy workers" gauge. Used as Executors.newThreadPerTaskExecutor(gauge).
 */
public final class WorkerGauge implements ThreadFactory {

    private final ThreadFactory threads;
    private final AtomicInteger busy = new AtomicInteger();

    /** Threads are virtual and named prefix0, prefix1, ... */
    public WorkerGauge(String prefix) {
        this.threads = Thread.ofVirtual().name(prefix, 0).factory();
    }

    /** An unstarted thread that runs the task. */
    @Override
    public Thread newThread(Runnable task) {
        busy.incrementAndGet();
        return threads.newThread(() -> {
            try {
                task.run();
            } finally {
                busy.decrementAndGet();
            }
        });
    }

    /** How many of this gauge's threads are running their task right now. */
    public int busy() {
        return busy.get();
    }
}
