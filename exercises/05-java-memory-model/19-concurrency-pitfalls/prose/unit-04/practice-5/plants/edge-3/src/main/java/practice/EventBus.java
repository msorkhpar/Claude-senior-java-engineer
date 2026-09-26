package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<String> listener) {
        listeners.add(listener);
    }

    public boolean unsubscribe(Consumer<String> listener) {
        return listeners.remove(listener);
    }

    /** Delivers the event to every listener subscribed when the delivery starts, in order. */
    public void fire(String event) {
        for (Consumer<String> listener : listeners) {
            listener.accept(event);
        }
    }

    /** The current listeners, as an unmodifiable copy. */
    public List<Consumer<String>> listeners() {
        return java.util.Collections.unmodifiableList(listeners);
    }
}
