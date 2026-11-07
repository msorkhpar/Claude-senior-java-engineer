package practice;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Hands each alert to the subscribers of an on-call system: the pager, the chat bridge, the
 * escalation timer. A subscriber may subscribe or unsubscribe others, or itself, while it handles
 * an alert: the escalation timer unsubscribes itself once someone acknowledges.
 */
public class AlertBus {

    public interface Subscriber {
        void deliver(String alert);
    }

    private final List<Subscriber> subscribers = new CopyOnWriteArrayList<>();

    public void subscribe(Subscriber s) {
        subscribers.add(s);
    }

    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }

    /**
     * Hands the alert to each subscriber, in the order they subscribed. A subscriber that is
     * unsubscribed before its turn comes doesn't get this alert. One subscribed while this alert is
     * being handed out starts with the next alert. Every other subscriber gets it exactly once.
     */
    public void publish(String alert) {
        for (Subscriber s : subscribers) {
            if (subscribers.contains(s)) s.deliver(alert);
        }
    }
}
