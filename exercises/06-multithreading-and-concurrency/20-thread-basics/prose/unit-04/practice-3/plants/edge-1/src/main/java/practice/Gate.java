package practice;

public final class Gate {

    private boolean open;

    /** Returns once the gate is open, waiting on this gate's monitor while it is closed. */
    public synchronized void pass() throws InterruptedException {
        while (!open) {
            wait(1);
        }
    }

    /** Opens the gate for good and wakes every waiting thread. */
    public synchronized void open() {
        open = true;
        notifyAll();
    }
}
