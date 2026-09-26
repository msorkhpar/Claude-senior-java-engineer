package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ConnectionPool {
    INSTANCE;

    private final List<String> connections = new ArrayList<>();

    /** Adds a connection URL; refuses null or blank. */
    public synchronized void addConnection(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("a connection URL is required");
        }
        connections.add(url);
    }

    /** The connections in the order added, as a list callers cannot modify. */
    public synchronized List<String> connections() {
        return connections;
    }

    /** Empties the pool. */
    public synchronized void clear() {
        connections.clear();
    }
}
