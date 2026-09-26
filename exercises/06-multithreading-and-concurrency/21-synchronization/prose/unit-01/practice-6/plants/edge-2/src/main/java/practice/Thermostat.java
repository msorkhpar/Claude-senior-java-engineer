package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

public final class Thermostat {

    private final Object lock = new Object();
    private final List<IntConsumer> listeners = new ArrayList<>();
    private int temperature;

    public void addListener(IntConsumer listener) {
        synchronized (lock) {
            listeners.add(listener);
        }
    }

    /** Stores the temperature under the lock, then calls every listener with it, outside the lock. */
    public void set(int temperature) {
        List<IntConsumer> toCall;
        synchronized (lock) {
            toCall = List.copyOf(listeners);
        }
        for (IntConsumer listener : toCall) {
            listener.accept(temperature);
        }
        synchronized (lock) {
            this.temperature = temperature;
        }
    }

    public int get() {
        synchronized (lock) {
            return temperature;
        }
    }
}
