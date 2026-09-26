package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

public final class Graph<T> {

    private final Map<T, Set<T>> adjacency = new LinkedHashMap<>();
    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    /** Adds both vertices if new, and the edge (both ways unless directed). */
    public void addEdge(T from, T to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        adjacency.computeIfAbsent(from, k -> new LinkedHashSet<>()).add(to);
        adjacency.computeIfAbsent(to, k -> new LinkedHashSet<>());
        adjacency.get(to).add(from);
    }

    /** Vertices reachable from start, in breadth-first order, each once. */
    public List<T> bfs(T start) {
        List<T> order = new ArrayList<>();
        if (!adjacency.containsKey(start)) {
            return order;
        }
        Set<T> seen = new HashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        seen.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            T current = queue.remove();
            order.add(current);
            for (T next : adjacency.get(current)) {
                if (seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return order;
    }

    /** A shortest path from 'from' to 'to', both included; empty when there is none. */
    public List<T> shortestPath(T from, T to) {
        if (!adjacency.containsKey(from) || !adjacency.containsKey(to)) {
            return List.of();
        }
        Map<T, T> parent = new HashMap<>();
        Set<T> seen = new HashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        seen.add(from);
        queue.add(from);
        while (!queue.isEmpty()) {
            T current = queue.remove();
            if (current.equals(to)) {
                List<T> path = new ArrayList<>();
                for (T at = to; at != null; at = parent.get(at)) {
                    path.add(at);
                }
                Collections.reverse(path);
                return path;
            }
            for (T next : adjacency.get(current)) {
                if (seen.add(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        return List.of();
    }
}
