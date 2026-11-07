package practice;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Call quotas of the public API, per client key. Billing tops a client up with grant(...), and
 * every request thread calls tryUse(...) before it serves a call. Many threads do both at once.
 */
public class Quotas {

    private final ConcurrentMap<String, Integer> remaining;

    public Quotas() {
        this(new ConcurrentHashMap<>());
    }

    /** Keeps the quotas in the given map, which the admin endpoints share. */
    public Quotas(ConcurrentMap<String, Integer> remaining) {
        this.remaining = remaining;
    }

    /** Calls the client may still make; 0 for a client that was never granted any. */
    public int remaining(String client) {
        return remaining.getOrDefault(client, 0);
    }

    /** Adds calls to the client's quota. */
    public void grant(String client, int calls) {
        remaining.merge(client, calls, Integer::sum);
    }

    /** Takes one call from the client's quota: true if it did, false (taking nothing) if none was left. */
    public boolean tryUse(String client) {
        boolean[] taken = {false};
        remaining.computeIfPresent(client, (c, left) -> {
            if (left == 0) return left;
            taken[0] = true;
            return left - 1;
        });
        return taken[0];
    }
}
