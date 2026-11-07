package practice;

/**
 * One sensor's readings for a day, in the order they arrived: a time (epoch milliseconds) and a
 * value each. The gateway keeps one log per sensor in memory, for tens of thousands of sensors,
 * and the dashboard queries the logs many times a minute.
 */
public class ReadingLog {

    private final int capacity;
    private int size;
    private final Reading[] readings;

    private record Reading(long time, double value) {}

    /** A log with room for {@code capacity} readings; the gateway never appends more. */
    public ReadingLog(int capacity) {
        this.capacity = capacity;
        this.readings = new Reading[capacity];
    }

    public int capacity() {
        return capacity;
    }

    public int size() {
        return size;
    }

    /** Records one reading behind the others. */
    public void append(long time, double value) {
        readings[size] = new Reading(time, value);
        size++;
    }

    /** The time of reading {@code i}, counted from 0 in arrival order. */
    public long timeAt(int i) {
        return readings[i].time();
    }

    /** The value of reading {@code i}, counted from 0 in arrival order. */
    public double valueAt(int i) {
        return readings[i].value();
    }

    /** The times of the readings whose value is above {@code limit}, in arrival order. */
    public long[] timesAbove(double limit) {
        int matches = 0;
        for (int i = 0; i < size; i++) if (readings[i].value() > limit) matches++;
        long[] result = new long[matches];
        for (int i = 0, j = 0; i < size; i++) if (readings[i].value() > limit) result[j++] = readings[i].time();
        return result;
    }
}
