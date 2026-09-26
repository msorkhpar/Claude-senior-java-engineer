package practice;

import java.util.List;
import java.util.function.Consumer;

public final class EventBus {

    public void subscribe(Consumer<String> listener) {
        throw new UnsupportedOperationException("write subscribe");
    }

    public boolean unsubscribe(Consumer<String> listener) {
        throw new UnsupportedOperationException("write unsubscribe");
    }

    /** Delivers the event to every listener subscribed when the delivery starts, in order. */
    public void fire(String event) {
        throw new UnsupportedOperationException("write fire");
    }

    /** The current listeners, as an unmodifiable copy. */
    public List<Consumer<String>> listeners() {
        throw new UnsupportedOperationException("write listeners");
    }
}
