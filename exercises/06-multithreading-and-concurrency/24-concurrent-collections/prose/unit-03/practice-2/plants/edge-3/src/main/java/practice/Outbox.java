package practice;

import java.util.Iterator;
import java.util.List;

/**
 * Messages waiting to be sent to partners. Each one has a deadline; one that has passed its
 * deadline is no longer worth sending. The service that owns the outbox chooses the list it
 * lives in: an ArrayList on the batch sender, a CopyOnWriteArrayList on the live one.
 */
public class Outbox {

    public record Message(String id, long deadline) {}

    private final List<Message> pending;

    public Outbox(List<Message> pending) {
        this.pending = pending;
    }

    public void add(Message message) {
        pending.add(message);
    }

    public List<String> pendingIds() {
        return pending.stream().map(Message::id).toList();
    }

    /**
     * Drops, from this outbox's own list, every message whose deadline is at or before now, keeps
     * the others in their order, and returns how many it dropped.
     */
    int dropExpired(long now) {
        int dropped = 0;
        for (Iterator<Message> it = pending.iterator(); it.hasNext(); ) {
            if (it.next().deadline() <= now) {
                it.remove();
                dropped++;
            }
        }
        return dropped;
    }
}
