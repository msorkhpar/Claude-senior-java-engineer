package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Notifier<T> {

    private final List<Consumer<T>> listeners = new ArrayList<>();

    /** Registers a listener. */
    public void addListener(Consumer<T> listener) {
        listeners.add(listener);
    }

    /** Hands the event to every listener, in the order they were added. */
    public void publish(T event) {
        for (Consumer<T> listener : listeners) {
            listener.accept(event);
        }
    }
}
