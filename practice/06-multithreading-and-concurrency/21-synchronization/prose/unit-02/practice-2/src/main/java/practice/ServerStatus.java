package practice;

public final class ServerStatus {

    public void markUp() {
        throw new UnsupportedOperationException("write markUp");
    }

    public void markDown() {
        throw new UnsupportedOperationException("write markDown");
    }

    public boolean isUp() {
        throw new UnsupportedOperationException("write isUp");
    }

    /** Counts one request served; many threads call it at once. */
    public void request() {
        throw new UnsupportedOperationException("write request");
    }

    public long requests() {
        throw new UnsupportedOperationException("write requests");
    }

    public void restartedAt(long millis) {
        throw new UnsupportedOperationException("write restartedAt");
    }

    public long lastRestart() {
        throw new UnsupportedOperationException("write lastRestart");
    }
}
