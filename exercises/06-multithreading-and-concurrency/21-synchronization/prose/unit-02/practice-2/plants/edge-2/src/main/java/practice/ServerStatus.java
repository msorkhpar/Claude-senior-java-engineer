package practice;

import java.util.concurrent.atomic.AtomicLong;

public final class ServerStatus {

    private volatile boolean up;
    private final AtomicLong requests = new AtomicLong();
    private  long lastRestart;

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
        requests.incrementAndGet();
    }

    public long requests() {
        return requests.get();
    }

    public void restartedAt(long millis) {
        lastRestart = millis;
    }

    public long lastRestart() {
        return lastRestart;
    }
}
