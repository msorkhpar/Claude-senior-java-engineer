package practice;

public final class Gate {

    /** Returns once the gate is open, waiting on this gate's monitor while it is closed. */
    public void pass() throws InterruptedException {
        throw new UnsupportedOperationException("write pass");
    }

    /** Opens the gate for good and wakes every waiting thread. */
    public void open() {
        throw new UnsupportedOperationException("write open");
    }
}
