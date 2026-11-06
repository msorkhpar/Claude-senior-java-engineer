package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ThumbnailWorkerTest {

    private static final List<List<String>> UPLOADS = List.of(
            List.of("beach-01.png", "beach-02.png", "beach-03.png", "beach-04.png"),
            List.of("dog-11.jpg", "dog-12.jpg"),
            List.of("receipt-7.tif", "receipt-8.tif", "receipt-9.tif"));

    private final List<String> rendered = Collections.synchronizedList(new ArrayList<>());

    @AfterEach
    void clearTheTestThreadsFlag() {
        Thread.interrupted();
    }

    /** Renders, and asks the current thread to stop while it renders {@code stopAt}. */
    private ThumbnailWorker.Renderer stoppingAt(String stopAt) {
        return image -> {
            rendered.add(image);
            if (image.equals(stopAt)) Thread.currentThread().interrupt();
        };
    }

    @Test
    void rendersEveryUploadWhenNobodyAsksItToStop() {
        int count = new ThumbnailWorker(rendered::add).renderAll(UPLOADS);

        assertThat(count).isEqualTo(9);
        assertThat(rendered).containsExactly("beach-01.png", "beach-02.png", "beach-03.png", "beach-04.png",
                "dog-11.jpg", "dog-12.jpg", "receipt-7.tif", "receipt-8.tif", "receipt-9.tif");
    }

    @Test
    void aRequestInTheMiddleOfABatchEndsThatBatch() {
        int count = new ThumbnailWorker(stoppingAt("beach-02.png")).renderAll(UPLOADS);

        assertThat(rendered).doesNotContain("beach-03.png", "beach-04.png");
        assertThat(count).isEqualTo(2);
    }

    @Test
    void aRequestInTheMiddleOfABatchAlsoSkipsTheBatchesAfterIt() {
        new ThumbnailWorker(stoppingAt("beach-02.png")).renderAll(UPLOADS);

        assertThat(rendered).doesNotContain("dog-11.jpg", "dog-12.jpg", "receipt-7.tif", "receipt-8.tif",
                "receipt-9.tif");
    }

    @Test
    void whoeverCalledItCanStillSeeTheRequest() {
        new ThumbnailWorker(stoppingAt("dog-12.jpg")).renderAll(UPLOADS);

        assertThat(Thread.currentThread().isInterrupted()).isTrue();
    }

    @Test
    void shuttingDownThePoolStopsAWorkerThatIsRendering() throws InterruptedException {
        CountDownLatch busy = new CountDownLatch(1);
        ThumbnailWorker.Renderer slowOnTheSecond = image -> {
            rendered.add(image);
            if (image.equals("beach-02.png")) {
                busy.countDown();
                while (!Thread.currentThread().isInterrupted()) Thread.onSpinWait();  // still rendering
            }
        };
        ExecutorService pool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        pool.execute(() -> new ThumbnailWorker(slowOnTheSecond).renderAll(UPLOADS));
        assertThat(busy.await(5, TimeUnit.SECONDS)).isTrue();

        pool.shutdownNow();

        assertThat(pool.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(rendered).containsExactly("beach-01.png", "beach-02.png");
    }
}
