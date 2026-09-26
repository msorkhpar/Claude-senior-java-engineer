package practice;

public final class SharedCell {

    private int value;

    public synchronized void set(int value) {
        this.value = value;
    }

    public synchronized int get() {
        return value;
    }

    /** Adds delta and returns the new value, as one atomic step. */
    public synchronized int addAndGet(int delta) {
        value += delta;
        return value;
    }
}
