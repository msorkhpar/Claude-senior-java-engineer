package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Collects audit events from one request thread into batches and hands each full batch to the
 * sender thread through the outbox. The sender takes batches from the outbox and ships them.
 */
public class AuditRecorder {

    private final BlockingQueue<List<String>> outbox;
    private final int batchSize;
    private List<String> batch = new ArrayList<>();

    public AuditRecorder(BlockingQueue<List<String>> outbox, int batchSize) {
        this.outbox = outbox;
        this.batchSize = batchSize;
    }

    /** Records one event; called only by the thread that owns this recorder. */
    public void record(String event) throws InterruptedException {
        batch.add(event);
        if (batch.size() == batchSize) handOff();
    }

    /** Hands over the events recorded since the last batch, if there are any. Called on shutdown. */
    public void flush() throws InterruptedException {
        if (!batch.isEmpty()) handOff();
    }

    /** Puts the current batch on the outbox and gets ready to record the next one. */
    void handOff() throws InterruptedException {
        outbox.put(batch);
        batch.clear();
    }
}
