package practice;

/**
 * Keeps one consumer thread working on the payments queue. A scheduler calls check() every few
 * seconds; the consumer's code is a Runnable that may die on a payment it can't handle.
 */
public class Supervisor {

    private final String name;
    private final Runnable consumer;
    private Thread worker;

    public Supervisor(String name, Runnable consumer) {
        this.name = name;
        this.consumer = consumer;
    }

    /** Called by the scheduler: makes sure the consumer is working, on exactly one thread. */
    public void check() {
        if (worker == null || !worker.isAlive()) {
            worker = newWorker();
            worker.start();
        }
    }

    /** The thread currently assigned to the consumer, or null before the first check. */
    public Thread worker() {
        return worker;
    }

    /** A daemon thread for the consumer, not started yet, named like every one before it. */
    Thread newWorker() {
        Thread t = new Thread(consumer, name);
        t.setDaemon(true);
        return t;
    }
}
