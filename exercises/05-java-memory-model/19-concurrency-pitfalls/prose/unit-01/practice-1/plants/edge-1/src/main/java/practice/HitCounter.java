package practice;

public final class HitCounter {

    private volatile int count;

    /** Adds one hit. */
    public void hit() {
        count++;
    }

    /** Returns the number of hits so far. */
    public synchronized int hits() {
        return count;
    }
}
