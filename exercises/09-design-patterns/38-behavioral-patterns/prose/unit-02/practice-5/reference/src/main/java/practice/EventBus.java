package practice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class EventBus {

    @FunctionalInterface
    public interface Subscription {
        void cancel();
    }

    private final Map<String, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    public Subscription subscribe(String eventType, Consumer<Object> listener) {
        requireType(eventType);
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(listener);
        return () -> unsubscribe(eventType, listener);
    }

    public void unsubscribe(String eventType, Consumer<Object> listener) {
        List<Consumer<Object>> list = eventType == null ? null : listeners.get(eventType);
        if (list != null) {
            list.remove(listener);
        }
    }

    public void publish(String eventType, Object data) {
        requireType(eventType);
        List<Consumer<Object>> list = listeners.get(eventType);
        if (list != null) {
            for (Consumer<Object> listener : list) {
                listener.accept(data);
            }
        }
    }

    public int listenerCount(String eventType) {
        List<Consumer<Object>> list = eventType == null ? null : listeners.get(eventType);
        return list == null ? 0 : list.size();
    }

    public Set<String> eventTypes() {
        return Set.copyOf(listeners.keySet());
    }

    private static void requireType(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("Event type cannot be null or blank");
        }
    }
}
