package practice;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
public final class SimpleEventBus {
    private final Map<String, Set<Consumer<Object>>> listeners = new ConcurrentHashMap<>();
    public void subscribe(String eventType, Consumer<Object> listener) { listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArraySet<>()).add(listener); }
    public void publish(String eventType, Object event) { var l = listeners.get(eventType); if (l == null) return; for (var c : l) c.accept(event); }
    public int listenerCount(String eventType) { var l = listeners.get(eventType); return l == null ? 0 : l.size(); }
}
