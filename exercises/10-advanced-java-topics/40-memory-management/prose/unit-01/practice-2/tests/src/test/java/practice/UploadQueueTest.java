package practice;

import org.junit.jupiter.api.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UploadQueueTest {

    private static UploadQueue.Upload upload(String name) {
        return new UploadQueue.Upload(name, new byte[256 * 1024]);
    }

    /** Puts uploads named prefix-0.. into the queue and returns weak references to their bodies. */
    private static List<WeakReference<byte[]>> fill(UploadQueue queue, String prefix, int n) {
        List<WeakReference<byte[]>> bodies = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            UploadQueue.Upload u = upload(prefix + "-" + i);
            bodies.add(new WeakReference<>(u.body()));
            queue.put(u);
        }
        return bodies;
    }

    /** Takes n uploads, scans them, and keeps nothing. */
    private static void scan(UploadQueue queue, int n) {
        for (int i = 0; i < n; i++) assertThat(queue.take()).isNotNull();
    }

    /** Collects until every reference is cleared, or gives up after twenty collections. */
    private static long stillHeld(List<WeakReference<byte[]>> bodies) {
        for (int i = 0; i < 20 && bodies.stream().anyMatch(r -> r.get() != null); i++) System.gc();
        return bodies.stream().filter(r -> r.get() != null).count();
    }

    @Test
    void handsOutUploadsOldestFirstAcrossGrowth() {
        UploadQueue queue = new UploadQueue();
        for (int i = 0; i < 5; i++) queue.put(upload("scan-" + i));
        assertThat(queue.take().name()).isEqualTo("scan-0");
        assertThat(queue.take().name()).isEqualTo("scan-1");
        for (int i = 5; i < 20; i++) queue.put(upload("scan-" + i));     // wraps, then grows

        List<String> names = new ArrayList<>();
        for (UploadQueue.Upload u; (u = queue.take()) != null; ) names.add(u.name());
        assertThat(names).hasSize(18);
        for (int i = 0; i < 18; i++) assertThat(names.get(i)).isEqualTo("scan-" + (i + 2));
        assertThat(queue.size()).isZero();
    }

    @Test
    void discardingEmptiesTheQueueAndItStillTakesNewUploads() {
        UploadQueue queue = new UploadQueue();
        for (int i = 0; i < 11; i++) queue.put(upload("old-" + i));
        queue.take();

        queue.clear();
        assertThat(queue.size()).isZero();
        assertThat(queue.take()).isNull();

        queue.put(upload("new-1"));
        queue.put(upload("new-2"));
        assertThat(queue.size()).isEqualTo(2);
        assertThat(queue.take().name()).isEqualTo("new-1");
        assertThat(queue.take().name()).isEqualTo("new-2");
        assertThat(queue.take()).isNull();
    }

    @Test
    void anUploadThatWasScannedIsNotHeldByTheQueue() {
        UploadQueue queue = new UploadQueue();
        List<WeakReference<byte[]>> bodies = fill(queue, "invoice", 6);
        scan(queue, 6);

        assertThat(stillHeld(bodies)).as("scanned uploads still held after collection").isZero();
        Reference.reachabilityFence(queue);
    }

    @Test
    void discardedUploadsAreNotHeldByTheQueue() {
        UploadQueue queue = new UploadQueue();
        List<WeakReference<byte[]>> bodies = fill(queue, "photo", 13);
        queue.clear();

        assertThat(stillHeld(bodies)).as("discarded uploads still held after collection").isZero();
        Reference.reachabilityFence(queue);
    }
}
