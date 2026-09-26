package practice;

public final class ReentrantMutex {

    private Thread owner;
    private int holds;

    /** Takes the mutex, waiting while another thread holds it; the holder may take it again. */
    public synchronized void lock() throws InterruptedException {
        Thread me = Thread.currentThread();
        while (owner != null) {
            wait();
        }
        owner = me;
        holds++;
    }

    /** Takes the mutex if no other thread holds it. */
    public synchronized boolean tryLock() {
        Thread me = Thread.currentThread();
        if (owner != null) {
            return false;
        }
        owner = me;
        holds++;
        return true;
    }

    /** Gives back one hold; the mutex is free at zero. */
    public synchronized void unlock() {
        if (owner != Thread.currentThread()) {
            throw new IllegalMonitorStateException("not the owner");
        }
        if (--holds == 0) {
            owner = null;
            notifyAll();
        }
    }

    /** The owner's hold count, 0 when free. */
    public synchronized int holdCount() {
        return holds;
    }

    public synchronized boolean isHeldByCurrentThread() {
        return owner == Thread.currentThread();
    }
}
