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
        int read = count;
        step.run();
        count = read + 1;
    }

    public int getCount() {
        return count;
    }
}
