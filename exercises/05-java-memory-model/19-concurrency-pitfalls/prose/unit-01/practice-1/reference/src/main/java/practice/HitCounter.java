package practice;

public final class HitCounter {

    private int count;

    /** Adds one hit. */
    public synchronized void hit() {
        count++;
    }

    /** Returns the number of hits so far. */
    public synchronized int hits() {
        return count;
    }
}
