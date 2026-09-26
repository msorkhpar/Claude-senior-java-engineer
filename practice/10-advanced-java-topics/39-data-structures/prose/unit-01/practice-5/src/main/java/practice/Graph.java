package practice;

import java.util.List;

public final class Graph<T> {

    public Graph(boolean directed) {
    }

    /** Adds both vertices if new, and the edge (both ways unless directed). */
    public void addEdge(T from, T to) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Vertices reachable from start, in breadth-first order, each once. */
    public List<T> bfs(T start) {
        throw new UnsupportedOperationException("TODO");
    }

    /** A shortest path from 'from' to 'to', both included; empty when there is none. */
    public List<T> shortestPath(T from, T to) {
        throw new UnsupportedOperationException("TODO");
    }
}
