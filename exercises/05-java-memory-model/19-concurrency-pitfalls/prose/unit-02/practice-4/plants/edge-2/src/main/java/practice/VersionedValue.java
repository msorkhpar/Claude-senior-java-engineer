package practice;

import java.util.concurrent.atomic.AtomicStampedReference;

public final class VersionedValue {

    private final AtomicStampedReference<String> ref;

    public VersionedValue(String initial) {
        this.ref = new AtomicStampedReference<>(initial, 0);
    }

    public String value() {
        return ref.getReference();
    }

    public int stamp() {
        return ref.getStamp();
    }

    /** Stores the value and adds one to the stamp. */
    public void set(String newValue) {
        int[] stamp = new int[1];
        String current;
        do {
            current = ref.get(stamp);
        } while (!ref.compareAndSet(current, newValue, stamp[0], stamp[0] + 1));
    }

    /** Succeeds only if both the value (by reference) and the stamp still match. */
    public boolean compareAndSet(String expectedValue, int expectedStamp, String newValue) {
        if (ref.compareAndSet(expectedValue, newValue, expectedStamp, expectedStamp + 1)) {
            return true;
        }
        // A failed attempt still records that it was tried.
        int[] stamp = new int[1];
        String current = ref.get(stamp);
        ref.set(current, stamp[0] + 1);
        return false;
    }
}
