package practice;

/**
 * Uploads waiting to be scanned, oldest first. The scanner thread takes them one at a time; an
 * operator can discard everything still waiting. Each upload carries its whole body, often
 * several megabytes, and the queue lives as long as the service.
 */
public class UploadQueue {

    /** One uploaded file. */
    public record Upload(String name, byte[] body) {}

    private Upload[] items = new Upload[8];
    private int head;      // index of the oldest upload
    private int count;     // uploads waiting

    /** Adds an upload behind the others. */
    public void put(Upload upload) {
        if (count == items.length) grow();
        items[(head + count) % items.length] = upload;
        count++;
    }

    /** How many uploads are waiting. */
    public int size() {
        return count;
    }

    private void grow() {
        Upload[] bigger = new Upload[items.length * 2];
        for (int i = 0; i < count; i++) bigger[i] = items[(head + i) % items.length];
        items = bigger;
        head = 0;
    }

    /** Removes and returns the oldest waiting upload, or null when none is waiting. */
    public Upload take() {
        // TODO: Hand out the oldest waiting upload, or null when none is waiting.
        throw new IllegalStateException("TODO: take");
    }

    /** Discards every waiting upload. */
    public void clear() {
        // TODO: Discard every waiting upload; the queue is then empty and takes new ones.
        throw new IllegalStateException("TODO: clear");
    }
}
