package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
        List<String> gone = new ArrayList<>();
        synchronized (lastSeen) {
            Iterator<Map.Entry<String, Long>> it = lastSeen.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Long> e = it.next();
                if (policy.expired(e.getKey(), e.getValue())) {
                    it.remove();
                    gone.add(e.getKey());
                }
            }
        }
        return gone;
    }

    /** The sessions as they are now, in a map of the caller's own that later touches don't change. */
    public Map<String, Long> snapshot() {
        synchronized (lastSeen) {
            return new HashMap<>(lastSeen);
        }
    }
}
