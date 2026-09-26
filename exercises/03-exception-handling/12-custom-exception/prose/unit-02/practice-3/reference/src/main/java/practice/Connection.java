package practice;

public class Connection {

    private boolean closed;

    /** Returns "sent: " + message while open; after close() throws IllegalStateException("Connection closed"). */
    public String send(String message) {
        if (closed) {
            throw new IllegalStateException("Connection closed");
        }
        return "sent: " + message;
    }

    public void close() {
        closed = true;
    }
}
