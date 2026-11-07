package practice;

/**
 * One sensor's readings for a day, in the order they arrived: a time (epoch milliseconds) and a
 * value each. The gateway keeps one log per sensor in memory, for tens of thousands of sensors,
 * and the dashboard queries the logs many times a minute.
 */
public class ReadingLog {

    private final int capacity;
    private int size;
    // TODO: Decide how the readings are stored. The gateway holds tens of thousands of these
    //       logs, so what one reading costs in memory decides how many sensors it can serve.

    /** A log with room for {@code capacity} readings; the gateway never appends more. */
    public ReadingLog(int capacity) {
        this.capacity = capacity;
        // TODO: Prepare the storage for {@code capacity} readings.
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return size;
    }

    /** Records one reading behind the others. */
    public void append(long time, double value) {
        // TODO: Record the reading.
        throw new IllegalStateException("TODO: append");
    }

    /** The time of reading {@code i}, counted from 0 in arrival order. */
    public long timeAt(int i) {
        // TODO
        throw new IllegalStateException("TODO: timeAt");
    }

    /** The value of reading {@code i}, counted from 0 in arrival order. */
    public double valueAt(int i) {
        // TODO
        throw new IllegalStateException("TODO: valueAt");
    }

    /** The times of the readings whose value is above {@code limit}, in arrival order. */
    public long[] timesAbove(double limit) {
        // TODO: The dashboard calls this many times a minute on every log.
        throw new IllegalStateException("TODO: timesAbove");
    }
}
