package practice;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Notifier<T> {

    private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

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
