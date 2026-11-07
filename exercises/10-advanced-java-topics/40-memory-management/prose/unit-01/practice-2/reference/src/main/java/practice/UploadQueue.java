package practice;

import java.util.Arrays;

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
        if (count == 0) return null;
        Upload oldest = items[head];
        items[head] = null;
        head = (head + 1) % items.length;
        count--;
        return oldest;
    }

    /** Discards every waiting upload. */
    public void clear() {
        Arrays.fill(items, null);
        head = 0;
        count = 0;
    }
}
