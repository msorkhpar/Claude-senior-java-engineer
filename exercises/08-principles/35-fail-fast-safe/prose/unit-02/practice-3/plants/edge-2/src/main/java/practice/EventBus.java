package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** A listener registry: listeners are added rarely, events are published often. */
public class EventBus<E> {

    private final List<Consumer<E>> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(Consumer<E> listener) {
        listeners.add(listener);
    }

    public void unsubscribe(Consumer<E> listener) {
        listeners.remove(listener);
    }

    public void publish(E event) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).accept(event);
        }
    }
}
