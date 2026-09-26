package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public final class States {

    private States() {
    }

    /** Returns a daemon thread that is in {@code state}, or soon will be, until the test lets it go. */
    public static Thread enter(Thread.State state, Object monitor, ReentrantLock lock, CountDownLatch done)
            throws InterruptedException {
        Runnable body = switch (state) {
            case NEW, TERMINATED -> () -> { };
            case RUNNABLE -> () -> {
                while (done.getCount() > 0) {
                    Thread.onSpinWait();
                }
            };
            case BLOCKED -> () -> {
                synchronized (monitor) {
                    done.countDown();
                }
            };
            case WAITING -> () -> {
                lock.lock();
                lock.unlock();
            };
            case TIMED_WAITING -> () -> {
                try {
                    done.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };
        };
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        if (state != Thread.State.NEW) {
            thread.start();
        }
        if (state == Thread.State.TERMINATED) {
            thread.join();
        }
        return thread;
    }
}
