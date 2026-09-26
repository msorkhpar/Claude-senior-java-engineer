package practice;

public class Connection {

    private IllegalStateException closedError;
    private boolean closed;

    /** Returns "sent: " + message while open; after close() throws IllegalStateException("Connection closed"). */
    public String send(String message) {
        if (closed) {
            if (closedError == null) {
                closedError = new IllegalStateException("Connection closed");
            }
            throw closedError;
        }
        return "sent: " + message;
    }

    public void close() {
        closed = true;
    }
}
