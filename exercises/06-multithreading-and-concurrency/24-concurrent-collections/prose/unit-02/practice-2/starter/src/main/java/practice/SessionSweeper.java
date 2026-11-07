package practice;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Login sessions: the time each session was last seen, in a synchronized map. Request threads
 * call touch(...) on every request; a sweeper thread calls sweep(...) every minute; the debug
 * endpoint calls snapshot() and writes the result out while requests go on.
 */
public class SessionSweeper {

    /** Decides whether a session has expired; it may look up the user, so it is not free. */
    public interface ExpiryPolicy {
        boolean expired(String sessionId, long lastSeen);
    }

    private final Map<String, Long> lastSeen = Collections.synchronizedMap(new HashMap<>());

    /** Records that a session was seen at the given time. */
    public void touch(String sessionId, long time) {
        lastSeen.put(sessionId, time);
    }

    /** The time the session was last seen, or null if it is not (or no longer) a session. */
    public Long lastSeen(String sessionId) {
        return lastSeen.get(sessionId);
    }

    /** Removes every session the policy says has expired, and returns their ids. */
    public List<String> sweep(ExpiryPolicy policy) {
        // TODO: Remove every session the policy says has expired, and return their ids. Request
        //       threads keep calling touch(...) while the sweep runs, on sessions old and new.
        throw new IllegalStateException("TODO: sweep");
    }

    /** The sessions as they are now, in a map of the caller's own that later touches don't change. */
    public Map<String, Long> snapshot() {
        // TODO: Return the sessions as they are now. The debug endpoint writes the result out
        //       slowly while requests go on, and what it writes must not change under it.
        throw new IllegalStateException("TODO: snapshot");
    }
}
