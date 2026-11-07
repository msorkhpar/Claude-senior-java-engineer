package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Tells interested parts of the system that a deploy happened. Parts subscribe at startup and
 * rarely after; deploy events are delivered from several threads. A subscriber may subscribe or
 * unsubscribe itself or others while an event is being delivered.
 */
public class DeployNotifier {

    private final List<Consumer<String>> subscribers = newSubscribers();

    /** Subscribes a listener; returns false, and changes nothing, if it is already subscribed. */
    public synchronized boolean subscribe(Consumer<String> subscriber) {
        if (subscribers.contains(subscriber)) return false;
        return subscribers.add(subscriber);
    }

    /** Unsubscribes a listener; returns whether it was subscribed. */
    public boolean unsubscribe(Consumer<String> subscriber) {
        return subscribers.remove(subscriber);
    }

    /**
     * Delivers the event to every subscriber that was subscribed when this call began, in the
     * order they subscribed, and returns how many it delivered to.
     */
    public int deploy(String event) {
        // TODO: Deliver the event as described above. Subscribers run inside this call and may
        //       subscribe or unsubscribe anyone, themselves included, while it runs.
        throw new IllegalStateException("TODO: deploy");
    }

    /** The list that holds the subscribers. */
    List<Consumer<String>> newSubscribers() {
        // TODO: Subscribers change a few times a day; events are delivered all day long, from
        //       several threads at once. Return the list to keep them in.
        throw new IllegalStateException("TODO: newSubscribers");
    }
}
