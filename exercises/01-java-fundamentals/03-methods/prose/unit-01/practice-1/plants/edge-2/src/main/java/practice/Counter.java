package practice;

public class Counter {

    private static int total;

    private int count;

    /** Adds one click to this counter and to the total. */
    public void click() {
        count++;
        total = count;
    }

    /** Returns this counter's clicks. */
    public int count() {
        return count;
    }

    /** Returns the clicks of every counter together. */
    public static int total() {
        return total;
    }

    /** Sets the total back to 0. */
    public static void resetTotal() {
        total = 0;
    }
}
