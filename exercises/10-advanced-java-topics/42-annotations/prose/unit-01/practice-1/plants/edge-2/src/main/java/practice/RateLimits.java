package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Per-route rate limits, loaded from the tree a JSON or YAML parser produces. */
public final class RateLimits {
    private final Map<String, List<Integer>> limits;

    private RateLimits(Map<String, List<Integer>> limits) {
        this.limits = limits;
    }

    /**
     * Loads the limits. {@code raw} maps a route to its node as the parser produced it: a list of
     * requests allowed per minute, one per customer tier (free, paid, ...).
     */
    public static RateLimits load(Map<String, Object> raw) {
        Map<String, List<Integer>> limits = new LinkedHashMap<>();
        for (Map.Entry<String, Object> route : raw.entrySet()) {
            limits.put(route.getKey(), integers(route.getKey(), route.getValue()));
        }
        return new RateLimits(limits);
    }

    /**
     * Turns one route's parsed node into the list of limits the rest of the service reads.
     * Bad configuration must stop the load, with an IllegalArgumentException that names the route.
     */
    static List<Integer> integers(String route, Object node) {
        if (!(node instanceof List<?> list)) {
            throw new IllegalArgumentException(route + ": expected a list of limits, got " + node);
        }
        if (!list.isEmpty() && !(list.get(0) instanceof Integer)) {
            throw new IllegalArgumentException(route + ": not a whole number: " + list.get(0));
        }
        @SuppressWarnings("unchecked")
        List<Integer> limits = (List<Integer>) list;
        return limits;
    }

    /** Requests per minute for this route and tier (0 is the free tier). */
    public int perMinute(String route, int tier) {
        return limits.get(route).get(tier);
    }

    /** The sum of the route's limits over every tier, for the capacity report. */
    public int capacity(String route) {
        int sum = 0;
        for (int limit : limits.get(route)) sum += limit;
        return sum;
    }
}
