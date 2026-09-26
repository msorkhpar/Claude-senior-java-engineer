package practice;

import java.util.function.Consumer;

/** A listener registry: listeners are added rarely, events are published often. */
public class EventBus<E> {

    public void subscribe(Consumer<E> listener) {
        throw new UnsupportedOperationException("write subscribe");
    }

    public void unsubscribe(Consumer<E> listener) {
        throw new UnsupportedOperationException("write unsubscribe");
    }

    public void publish(E event) {
        throw new UnsupportedOperationException("write publish");
    }
}
