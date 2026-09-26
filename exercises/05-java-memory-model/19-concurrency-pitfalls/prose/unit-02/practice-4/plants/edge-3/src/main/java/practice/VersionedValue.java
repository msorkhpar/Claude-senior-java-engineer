package practice;

import java.util.Objects;

public final class VersionedValue {

    private String value;
    private int stamp;

    public VersionedValue(String initial) {
        this.value = initial;
    }

    public synchronized String value() {
        return value;
    }

    public synchronized int stamp() {
        return stamp;
    }

    /** Stores the value and adds one to the stamp. */
    public synchronized void set(String newValue) {
        value = newValue;
        stamp++;
    }

    /** Succeeds only if both the value (by reference) and the stamp still match. */
    public synchronized boolean compareAndSet(String expectedValue, int expectedStamp, String newValue) {
        if (!Objects.equals(value, expectedValue) || stamp != expectedStamp) {
            return false;
        }
        value = newValue;
        stamp++;
        return true;
    }
}
