package practice;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Listeners {

    private final CopyOnWriteArrayList<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    /** Adds the listener; false if it is already registered. */
    public boolean register(Consumer<String> listener) {
        return listeners.addIfAbsent(listener);
    }

    /** Removes the listener; whether it was registered. */
    public boolean unregister(Consumer<String> listener) {
        return listeners.remove(listener);
    }

    /** Passes event to every listener registered when the call began; how many were called. */
    public int fire(String event) {
        int called = 0;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).accept(event);
            called++;
        }
        return called;
    }
}
