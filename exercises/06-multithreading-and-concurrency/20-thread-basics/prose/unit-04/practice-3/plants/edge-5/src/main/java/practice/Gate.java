package practice;

public final class Gate {

    private final Object lock = new Object();
    private boolean open;

    public void pass() throws InterruptedException {
        synchronized (lock) {
            while (!open) {
                lock.wait();
            }
        }
    }

    public void open() {
        synchronized (lock) {
            open = true;
            lock.notifyAll();
        }
    }
}
