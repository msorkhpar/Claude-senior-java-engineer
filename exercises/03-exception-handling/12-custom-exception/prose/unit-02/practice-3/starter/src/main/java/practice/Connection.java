package practice;

public class Connection {

    /** Returns "sent: " + message while open; after close() throws IllegalStateException("Connection closed"). */
    public String send(String message) {
        throw new UnsupportedOperationException("write send");
    }

    public void close() {
        throw new UnsupportedOperationException("write close");
    }
}
