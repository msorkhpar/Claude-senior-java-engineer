package practice;

import java.util.Set;
import java.util.function.Consumer;

public final class EventBus {

    @FunctionalInterface
    public interface Subscription {
        void cancel();
    }

    public Subscription subscribe(String eventType, Consumer<Object> listener) {
        throw new UnsupportedOperationException("TODO");
    }

    public void unsubscribe(String eventType, Consumer<Object> listener) {
        throw new UnsupportedOperationException("TODO");
    }

    public void publish(String eventType, Object data) {
        throw new UnsupportedOperationException("TODO");
    }

    public int listenerCount(String eventType) {
        throw new UnsupportedOperationException("TODO");
    }

    public Set<String> eventTypes() {
        throw new UnsupportedOperationException("TODO");
    }
}
