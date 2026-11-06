package practice;

import java.util.List;

/**
 * Renders thumbnails for uploaded images, one batch of uploads after another. It runs on a
 * thread of the service's render pool, which is shut down with shutdownNow() on every deploy.
 */
public class ThumbnailWorker {

    /** Renders one image; pure CPU work that never blocks. */
    public interface Renderer {
        void render(String image);
    }

    private final Renderer renderer;

    public ThumbnailWorker(Renderer renderer) {
        this.renderer = renderer;
    }

    /** Renders the batches in order and returns how many images it rendered. */
    public int renderAll(List<List<String>> batches) {
        int rendered = 0;
        for (List<String> batch : batches) {
            if (stopRequested()) break;
            rendered += renderBatch(batch);
        }
        return rendered;
    }

    /** Renders one batch and returns how many of its images it rendered. */
    int renderBatch(List<String> batch) {
        int rendered = 0;
        for (String image : batch) {
            if (stopRequested()) break;
            renderer.render(image);
            rendered++;
        }
        return rendered;
    }

    /** Whether this worker has been asked to stop. */
    boolean stopRequested() {
        return Thread.currentThread().isInterrupted();
    }
}
