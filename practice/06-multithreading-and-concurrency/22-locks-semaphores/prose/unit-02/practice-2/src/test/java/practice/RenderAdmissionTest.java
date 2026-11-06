package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RenderAdmissionTest {

    private final ExecutorService callers = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void cleanUp() {
        callers.shutdownNow();
        Thread.interrupted();
    }

    private static void waitFor(java.util.function.BooleanSupplier condition) {
        long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!condition.getAsBoolean() && System.nanoTime() < until) Thread.onSpinWait();
    }

    @Test
    void previewsAndBatchDocumentsRenderWhenRenderersAreFree() throws Exception {
        Semaphore renderers = new Semaphore(3, true);
        RenderAdmission admission = new RenderAdmission(renderers, 5_000);
        AtomicInteger rendered = new AtomicInteger();

        assertThat(admission.preview(rendered::incrementAndGet)).isTrue();
        assertThat(admission.batch(rendered::incrementAndGet)).isTrue();
        assertThat(admission.preview(rendered::incrementAndGet)).isTrue();

        assertThat(rendered.get()).isEqualTo(3);
        assertThat(renderers.availablePermits()).isEqualTo(3);
    }

    /** A renderer that another scheduler instance takes in the moment after anyone reads the count. */
    static final class Contended extends Semaphore {
        final AtomicBoolean armed = new AtomicBoolean(true);

        Contended(int permits) {
            super(permits, true);
        }

        @Override
        public int availablePermits() {
            int now = super.availablePermits();
            if (now > 0 && armed.getAndSet(false)) super.tryAcquire();
            return now;
        }
    }

    @Test
    void aBatchDocumentNeverWaitsEvenWhenARendererIsTakenUnderIt() throws Exception {
        RenderAdmission admission = new RenderAdmission(new Contended(1), 30_000);

        Future<Boolean> batch = callers.submit(() -> admission.batch(() -> { }));

        assertThat(batch.get(2, TimeUnit.SECONDS)).isNotNull();
    }

    @Test
    void aBatchDocumentIsRefusedAtOnceWhenEveryRendererIsBusy() throws Exception {
        Semaphore renderers = new Semaphore(2, true);
        RenderAdmission admission = new RenderAdmission(renderers, 30_000);
        renderers.acquire(2);
        AtomicBoolean ran = new AtomicBoolean();

        Future<Boolean> batch = callers.submit(() -> admission.batch(() -> ran.set(true)));

        assertThat(batch.get(2, TimeUnit.SECONDS)).isFalse();
        assertThat(ran).isFalse();
    }

    @Test
    void aBatchDocumentDoesNotTakeARendererSomeoneIsQueuedFor() throws Exception {
        Semaphore renderers = new Semaphore(1, true);
        RenderAdmission admission = new RenderAdmission(renderers, 30_000);
        // a caller queued for renderers; it needs two, so it stays queued while one is free
        Future<?> queued = callers.submit(() -> {
            renderers.acquire(2);
            return null;
        });
        waitFor(renderers::hasQueuedThreads);
        assertThat(renderers.hasQueuedThreads()).isTrue();
        AtomicBoolean ran = new AtomicBoolean();

        Future<Boolean> batch = callers.submit(() -> admission.batch(() -> ran.set(true)));

        assertThat(batch.get(2, TimeUnit.SECONDS)).isFalse();
        assertThat(ran).isFalse();
        assertThat(renderers.availablePermits()).isEqualTo(1);
        queued.cancel(true);
    }

    @Test
    void aPreviewGetsARendererThatFreesUpWhileItWaits() throws Exception {
        Semaphore renderers = new Semaphore(1, true);
        RenderAdmission admission = new RenderAdmission(renderers, 10_000);
        renderers.acquire();
        AtomicBoolean ran = new AtomicBoolean();

        Future<Boolean> preview = callers.submit(() -> admission.preview(() -> ran.set(true)));
        waitFor(() -> renderers.hasQueuedThreads() || preview.isDone());
        renderers.release();

        assertThat(preview.get(5, TimeUnit.SECONDS)).isTrue();
        assertThat(ran).isTrue();
    }

    @Test
    void aPreviewGivesUpWhenNoRendererFreesUpInTime() throws Exception {
        Semaphore renderers = new Semaphore(1, true);
        RenderAdmission admission = new RenderAdmission(renderers, 100);
        renderers.acquire();
        AtomicBoolean ran = new AtomicBoolean();

        Future<Boolean> preview = callers.submit(() -> admission.preview(() -> ran.set(true)));

        assertThat(preview.get(5, TimeUnit.SECONDS)).isFalse();
        assertThat(ran).isFalse();
    }
}
