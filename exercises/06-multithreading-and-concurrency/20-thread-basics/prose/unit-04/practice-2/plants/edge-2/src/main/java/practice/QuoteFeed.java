package practice;

import java.util.concurrent.TimeUnit;

/**
 * The connection to a market-data feed. The feed's own thread calls reconnect() when the
 * connection drops; the monitoring threads call status() every second; shutdown calls close().
 */
public class QuoteFeed {

    /** One attempt to connect; returns whether it succeeded. */
    public interface Connector {
        boolean connect(int attempt);
    }

    private final Connector connector;
    private final long backoffMillis;
    private boolean connected;
    private boolean closed;
    private int attempts;

    public QuoteFeed(Connector connector, long backoffMillis) {
        this.connector = connector;
        this.backoffMillis = backoffMillis;
    }

    /** Tries to connect, backing off between attempts, until it succeeds or the feed is closed. */
    public synchronized boolean reconnect() throws InterruptedException {
        connected = false;
        while (!closed) {
            attempts++;
            if (connector.connect(attempts)) {
                connected = true;
                return true;
            }
            pause(backoffMillis);
        }
        return false;
    }

    /** What the monitoring page shows for this feed. */
    public synchronized String status() {
        return (closed ? "closed" : connected ? "connected" : "reconnecting") + ", attempts: " + attempts;
    }

    /** Closes the feed for good; a reconnect in progress gives up. */
    public synchronized void close() {
        closed = true;
    }

    /**
     * The back-off between two attempts: returns after {@code millis}, or sooner once the feed
     * is closed. Called from reconnect(), with this feed's monitor held.
     */
    void pause(long millis) throws InterruptedException {
        long left = TimeUnit.MILLISECONDS.toNanos(millis);
        long end = System.nanoTime() + left;
        while (!closed && left > 0) {
            TimeUnit.NANOSECONDS.timedWait(this, left);
            left = end - System.nanoTime();
        }
    }
}
