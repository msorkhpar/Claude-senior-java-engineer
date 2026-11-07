package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects metric lines between two shipments. Every ten seconds a shipper thread calls drain()
 * and sends the batch it returns to the collector; recording goes on into the next batch.
 */
public class MetricsBuffer {

    private List<String> pending = new ArrayList<>();

    /** Adds one line to the batch being collected. */
    public synchronized void record(String line) {
        pending.add(line);
    }

    /** How many lines wait for the next drain. */
    public synchronized int pendingCount() {
        return pending.size();
    }

    /** Hands over the lines recorded since the last drain, and starts a new batch. */
    public synchronized List<String> drain() {
        // TODO: Return the batch collected so far, and leave the buffer collecting a new one.
        //       The shipper sends the returned list after this method returns, while record()
        //       goes on adding lines.
        throw new IllegalStateException("TODO: drain");
    }
}
