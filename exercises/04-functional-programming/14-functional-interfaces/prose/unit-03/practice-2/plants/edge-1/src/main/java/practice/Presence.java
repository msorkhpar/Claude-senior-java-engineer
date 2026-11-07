package practice;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks who is online in a chat service. A user can have several connections open at once
 * (phone, laptop, a second tab), and counts as online while at least one of them is open.
 * Connection events arrive on many threads, and the gateway sometimes repeats a disconnect.
 */
public class Presence {

    private final Map<String, Integer> openConnections = new ConcurrentHashMap<>();

    /** The users who are online now, in name order. */
    public SortedSet<String> online() {
        return new TreeSet<>(openConnections.keySet());
    }

    /** How many connections the user has open now. */
    public int connectionsOf(String user) {
        return openConnections.getOrDefault(user, 0);
    }

    /** One more connection of this user is open. */
    void connect(String user) {
        openConnections.merge(user, 1, Integer::sum);
    }

    /** One connection of this user has closed. */
    void disconnect(String user) {
        openConnections.computeIfPresent(user, (name, open) -> open - 1);
    }
}
