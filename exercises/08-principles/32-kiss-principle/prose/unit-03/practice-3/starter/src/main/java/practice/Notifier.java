package practice;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class Notifier<T> {

    private final CopyOnWriteArrayList<Consumer<T>> listeners = new CopyOnWriteArrayList<>();

    /** Registers a listener. */
    public void addListener(Consumer<T> listener) {
        throw new UnsupportedOperationException("write addListener");
    }

    /** Hands the event to every listener, in the order they were added. */
    public void publish(T event) {
        throw new UnsupportedOperationException("write publish");
    }
}
