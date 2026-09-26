package practice;

import java.util.function.Consumer;

public final class Listeners {

    /** Adds the listener; false if it is already registered. */
    public boolean register(Consumer<String> listener) {
        throw new UnsupportedOperationException("write register");
    }

    /** Removes the listener; whether it was registered. */
    public boolean unregister(Consumer<String> listener) {
        throw new UnsupportedOperationException("write unregister");
    }

    /** Passes event to every listener registered when the call began; how many were called. */
    public int fire(String event) {
        throw new UnsupportedOperationException("write fire");
    }
}
