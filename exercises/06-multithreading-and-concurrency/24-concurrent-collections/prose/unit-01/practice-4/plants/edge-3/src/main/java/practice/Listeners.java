package practice;

import java.util.ArrayList;
import java.util.function.Consumer;

public final class Listeners {

    private final ArrayList<Consumer<String>> listeners = new ArrayList<>();

    /** Adds the listener; false if it is already registered. */
    public boolean register(Consumer<String> listener) {
        return !listeners.contains(listener) && listeners.add(listener);
    }

    /** Removes the listener; whether it was registered. */
    public boolean unregister(Consumer<String> listener) {
        return listeners.remove(listener);
    }

    /** Passes event to every listener registered when the call began; how many were called. */
    public int fire(String event) {
        int called = 0;
        for (Consumer<String> listener : listeners) {
            listener.accept(event);
            called++;
        }
        return called;
    }
}
