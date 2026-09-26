package practice;

public final class SharedCell {

    public void set(int value) {
        throw new UnsupportedOperationException("write set");
    }

    public int get() {
        throw new UnsupportedOperationException("write get");
    }

    /** Adds delta and returns the new value, as one atomic step. */
    public int addAndGet(int delta) {
        throw new UnsupportedOperationException("write addAndGet");
    }
}
