package practice;



public final class ServerStatus {

    private volatile boolean up;
    private volatile long requests;
    private volatile long lastRestart;

    public void markUp() {
        up = true;
    }

    public void markDown() {
        up = false;
    }

    public boolean isUp() {
        return up;
    }

    /** Counts one request served; many threads call it at once. */
    public void request() {
        requests++;
    }

    public long requests() {
        return requests;
    }

    public void restartedAt(long millis) {
        lastRestart = millis;
    }

    public long lastRestart() {
        return lastRestart;
    }
}
