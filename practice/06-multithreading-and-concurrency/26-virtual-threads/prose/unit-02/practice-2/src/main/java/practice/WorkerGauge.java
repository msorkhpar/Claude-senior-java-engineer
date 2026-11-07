package practice;

import java.util.concurrent.ThreadFactory;

/**
 * Makes the threads that handle requests, and reports how many of them are busy for the
 * dashboard's "busy workers" gauge. Used as Executors.newThreadPerTaskExecutor(gauge).
 */
public final class WorkerGauge implements ThreadFactory {

    private final ThreadFactory threads;

    /** Threads are virtual and named prefix0, prefix1, ... */
    public WorkerGauge(String prefix) {
        this.threads = Thread.ofVirtual().name(prefix, 0).factory();
    }

    /** An unstarted thread that runs the task. */
    @Override
    public Thread newThread(Runnable task) {
        // TODO: a thread from `threads` that runs the task, so that busy() can report it.
        throw new IllegalStateException("TODO: newThread");
    }

    /** How many of this gauge's threads are running their task right now. */
    public int busy() {
        // TODO: the number of this gauge's threads running their task now.
        throw new IllegalStateException("TODO: busy");
    }
}
