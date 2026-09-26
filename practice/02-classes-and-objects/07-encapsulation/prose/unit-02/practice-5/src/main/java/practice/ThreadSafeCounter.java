package practice;

public class ThreadSafeCounter {

    private final Runnable step;
    private int count;

    public ThreadSafeCounter() {
        this(() -> { });
    }

    /** {@code step} runs inside every increment, between reading the count and writing it back. */
    public ThreadSafeCounter(Runnable step) {
        this.step = step;
    }

    public void incrementCount() {
        throw new UnsupportedOperationException("write incrementCount");
    }

    public int getCount() {
        throw new UnsupportedOperationException("write getCount");
    }
}
