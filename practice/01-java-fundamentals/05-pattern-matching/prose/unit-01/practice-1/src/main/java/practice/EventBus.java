package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Delivers events inside one service. A handler subscribes to a type, a class or an interface,
 * and receives every published event that belongs to that type.
 */
public class EventBus {

    private record Subscription(Class<?> type, Consumer<Object> handler) {
    }

    private final List<Subscription> subscriptions = new ArrayList<>();

    public <T> void subscribe(Class<T> type, Consumer<? super T> handler) {
        subscriptions.add(new Subscription(type, event -> handler.accept(type.cast(event))));
    }

    /** Delivers the event to every handler whose type it belongs to, in subscription order; returns how many. */
    public int publish(Object event) {
        int delivered = 0;
        for (Subscription subscription : subscriptions) {
            if (accepts(subscription.type(), event)) {
                subscription.handler().accept(event);
                delivered++;
            }
        }
        return delivered;
    }

    /** At startup: the event classes that no subscribed handler would receive, in the order given. */
    public List<Class<?>> unhandled(List<Class<?>> eventClasses) {
        List<Class<?>> missing = new ArrayList<>();
        for (Class<?> eventClass : eventClasses) {
            boolean handled = false;
            for (Subscription subscription : subscriptions) {
                if (handles(subscription.type(), eventClass)) {
                    handled = true;
                }
            }
            if (!handled) {
                missing.add(eventClass);
            }
        }
        return missing;
    }

    /** Whether a handler subscribed to {@code type} receives this event. */
    boolean accepts(Class<?> type, Object event) {
        // TODO: Decide from the event object. A message the decoder can't map arrives as null,
        //       and publishing it must reach no handler rather than fail.
        throw new IllegalStateException("TODO: accepts");
    }

    /** Whether a handler subscribed to {@code type} would receive events of class {@code eventClass}. */
    boolean handles(Class<?> type, Class<?> eventClass) {
        // TODO: The same decision as accepts(), made from a class before any event exists, so the
        //       startup check and publish() never disagree.
        throw new IllegalStateException("TODO: handles");
    }
}
