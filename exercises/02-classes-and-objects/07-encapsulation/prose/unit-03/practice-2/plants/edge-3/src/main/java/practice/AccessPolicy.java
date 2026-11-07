package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Which actions each role may perform. The authorizer keeps one policy for as long as the
 * service runs and asks it on every request.
 */
public final class AccessPolicy {

    private final Map<String, List<String>> actionsByRole;

    public AccessPolicy(Map<String, List<String>> actionsByRole) {
        this.actionsByRole = frozenCopy(actionsByRole);
    }

    public boolean allows(String role, String action) {
        return actionsFor(role).contains(action);
    }

    /** The actions of one role, in the order they were given; empty for a role it doesn't know. */
    public List<String> actionsFor(String role) {
        return actionsByRole.getOrDefault(role, List.of());
    }

    public Set<String> roles() {
        return actionsByRole.keySet();
    }

    /**
     * The table this policy keeps, built from what the caller passed in.
     */
    static Map<String, List<String>> frozenCopy(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new HashMap<>();
        source.forEach((role, actions) -> copy.put(role, List.copyOf(actions)));
        return copy;
    }
}
