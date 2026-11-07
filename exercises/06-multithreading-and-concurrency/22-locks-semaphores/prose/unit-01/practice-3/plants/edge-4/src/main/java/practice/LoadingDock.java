package practice;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The loading dock of a warehouse. Trucks wait for a free bay; at night the yard manager closes
 * the gate once the last truck has left.
 */
public class LoadingDock {

    final ReentrantLock lock = new ReentrantLock();
    final Condition bayFree = lock.newCondition();
    final Condition dockEmpty = lock.newCondition();
    private final int bays;
    private int occupied;

    public LoadingDock(int bays) {
        this.bays = bays;
    }

    /** A truck takes a bay, waiting until one is free. */
    public void arrive() throws InterruptedException {
        lock.lock();
        try {
            waitForBay();
            occupied++;
        } finally {
            lock.unlock();
        }
    }

    /** A truck gives its bay back. */
    public void leave() {
        lock.lock();
        try {
            occupied--;
            announceLeave();
        } finally {
            lock.unlock();
        }
    }

    /** Returns once no truck is at the dock; the night close calls it before locking the gate. */
    public void awaitEmpty() throws InterruptedException {
        lock.lock();
        try {
            waitForEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int occupied() {
        lock.lock();
        try {
            return occupied;
        } finally {
            lock.unlock();
        }
    }

    /** Called with the lock held: returns once this truck can take a bay. */
    void waitForBay() throws InterruptedException {
        while (occupied == bays) bayFree.await();
    }

    /** Called with the lock held, after a truck has left. */
    void announceLeave() {
        bayFree.signal();
        if (occupied == 0) dockEmpty.signalAll();
    }

    /** Called with the lock held: returns once the dock is empty. */
    void waitForEmpty() throws InterruptedException {
        if (occupied > 0) dockEmpty.await();
    }
}
