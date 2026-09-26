package practice;

import java.util.function.IntConsumer;

public final class Thermostat {

    public void addListener(IntConsumer listener) {
        throw new UnsupportedOperationException("write addListener");
    }

    /** Stores the temperature under the lock, then calls every listener with it, outside the lock. */
    public void set(int temperature) {
        throw new UnsupportedOperationException("write set");
    }

    public int get() {
        throw new UnsupportedOperationException("write get");
    }
}
