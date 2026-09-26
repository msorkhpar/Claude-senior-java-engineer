package practice;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class VersionedValue {

    private final AtomicReference<String> ref;
    private final AtomicInteger stamp = new AtomicInteger();

    public VersionedValue(String initial) {
        this.ref = new AtomicReference<>(initial);
    }

    public String value() {
        return ref.get();
    }

    public int stamp() {
        return stamp.get();
    }

    /** Stores the value and adds one to the stamp. */
    public void set(String newValue) {
        ref.set(newValue);
        stamp.incrementAndGet();
    }

    /** Succeeds only if both the value (by reference) and the stamp still match. */
    public boolean compareAndSet(String expectedValue, int expectedStamp, String newValue) {
        if (ref.compareAndSet(expectedValue, newValue)) {
            stamp.incrementAndGet();
            return true;
        }
        return false;
    }
}
