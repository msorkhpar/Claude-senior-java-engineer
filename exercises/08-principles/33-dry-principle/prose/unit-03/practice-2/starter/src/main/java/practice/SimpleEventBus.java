package practice;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** One publish-subscribe mechanism with one kind of thread safety, shared by every component. */
public final class SimpleEventBus {

    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    /** Adds a listener for this event type. */
    public void subscribe(String eventType, Consumer<Object> listener) {
        throw new UnsupportedOperationException("write subscribe");
    }

    /** Hands the event to every listener of its type, in the order they subscribed. */
    public void publish(String eventType, Object event) {
        throw new UnsupportedOperationException("write publish");
    }

    /** How many listeners this event type has. */
    public int listenerCount(String eventType) {
        throw new UnsupportedOperationException("write listenerCount");
    }
}
