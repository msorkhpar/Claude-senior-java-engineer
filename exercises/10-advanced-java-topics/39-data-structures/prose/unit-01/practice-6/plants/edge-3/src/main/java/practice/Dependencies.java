package practice;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Dependencies {

    private final Map<String, Set<String>> edges = new LinkedHashMap<>();

    /** Records a directed edge from 'from' to 'to'. */
    public void addEdge(String from, String to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        edges.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
        edges.computeIfAbsent(to, k -> new LinkedHashSet<>());
    }

    /** True when some path of edges leads back to a vertex it started from. */
    public boolean hasCycle() {
        Set<String> visited = new HashSet<>();
        Set<String> onPath = new HashSet<>();
        if (edges.isEmpty()) {
            return false;
        }
        String first = edges.keySet().iterator().next();
        return reachesCycle(first, visited, onPath);
    }

    private boolean reachesCycle(String vertex, Set<String> visited, Set<String> onPath) {
        if (onPath.contains(vertex)) {
            return true;
        }
        if (!visited.add(vertex)) {
            return false;
        }
        onPath.add(vertex);
        for (String next : edges.get(vertex)) {
            if (reachesCycle(next, visited, onPath)) {
                return true;
            }
        }
        onPath.remove(vertex);
        return false;
    }
}
