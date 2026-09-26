package practice;

import java.util.List;

public enum ConnectionPool {
    INSTANCE;

    /** Adds a connection URL; refuses null or blank. */
    public void addConnection(String url) {
        throw new UnsupportedOperationException("write addConnection");
    }

    /** The connections in the order added, as a list callers cannot modify. */
    public List<String> connections() {
        throw new UnsupportedOperationException("write connections");
    }

    /** Empties the pool. */
    public void clear() {
        throw new UnsupportedOperationException("write clear");
    }
}
