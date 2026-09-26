package practice;

public class Connection {

    private static final IllegalStateException CLOSED = new IllegalStateException("Connection closed");

    private boolean closed;

    /** Returns "sent: " + message while open; after close() throws IllegalStateException("Connection closed"). */
    public String send(String message) {
        if (closed) {
            throw CLOSED;
        }
        return "sent: " + message;
    }

    public void close() {
        closed = true;
    }
}
