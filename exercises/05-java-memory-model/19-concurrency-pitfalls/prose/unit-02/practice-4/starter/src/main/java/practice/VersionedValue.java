package practice;

public final class VersionedValue {

    public VersionedValue(String initial) {
    }

    public String value() {
        throw new UnsupportedOperationException("write value");
    }

    public int stamp() {
        throw new UnsupportedOperationException("write stamp");
    }

    /** Stores the value and adds one to the stamp. */
    public void set(String newValue) {
        throw new UnsupportedOperationException("write set");
    }

    /** Succeeds only if both the value (by reference) and the stamp still match. */
    public boolean compareAndSet(String expectedValue, int expectedStamp, String newValue) {
        throw new UnsupportedOperationException("write compareAndSet");
    }
}
