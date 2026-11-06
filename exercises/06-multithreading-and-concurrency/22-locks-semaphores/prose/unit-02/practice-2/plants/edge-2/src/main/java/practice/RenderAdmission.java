package practice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Admits work to the PDF render farm, which has a fixed number of renderers. Interactive previews
 * come from users looking at a screen; batch documents come from the nightly scheduler, which
 * retries anything refused a few minutes later.
 */
public class RenderAdmission {

    private final Semaphore renderers;
    private final long previewWaitMillis;

    /** The renderers' semaphore must be fair: previews are served in the order they arrive. */
    RenderAdmission(Semaphore renderers, long previewWaitMillis) {
        this.renderers = renderers;
        this.previewWaitMillis = previewWaitMillis;
    }

    public RenderAdmission(int renderers, long previewWaitMillis) {
        this(new Semaphore(renderers, true), previewWaitMillis);
    }

    /** Renders a preview; false when no renderer could be had for it. */
    public boolean preview(Runnable render) throws InterruptedException {
        if (!admitPreview()) return false;
        try {
            render.run();
            return true;
        } finally {
            renderers.release();
        }
    }

    /** Renders a batch document; false when it was refused and the scheduler should retry. */
    public boolean batch(Runnable render) throws InterruptedException {
        if (!admitBatch()) return false;
        try {
            render.run();
            return true;
        } finally {
            renderers.release();
        }
    }

    /** Takes a renderer for a preview. True when one was taken. */
    boolean admitPreview() throws InterruptedException {
        return renderers.tryAcquire(previewWaitMillis, TimeUnit.MILLISECONDS);
    }

    /** Takes a renderer for a batch document. True when one was taken. */
    boolean admitBatch() throws InterruptedException {
        return renderers.tryAcquire(previewWaitMillis, TimeUnit.MILLISECONDS);
    }
}
