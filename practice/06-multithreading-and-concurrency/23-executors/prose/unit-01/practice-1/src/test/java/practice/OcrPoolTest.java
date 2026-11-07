package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OcrPoolTest {

    /** Holds every "long" scan until the test ends. */
    private final CountDownLatch endOfTest = new CountDownLatch(1);
    /** Submits scans from a thread of its own, named like an upload thread. */
    private ExecutorService uploader;
    private ThreadPoolExecutor pool;

    @BeforeEach
    void startUploader() {
        uploader = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "uploader");
            t.setDaemon(true);
            return t;
        });
    }

    @AfterEach
    void stopEverything() {
        endOfTest.countDown();
        if (pool != null) pool.shutdownNow();
        uploader.shutdownNow();
    }

    /** Builds the reader's pool on the uploader thread, so a create() that blocks can't hang the test. */
    private ThreadPoolExecutor build() throws Exception {
        pool = uploader.submit(OcrPool::create).get(5, TimeUnit.SECONDS);
        return pool;
    }

    /** Hands one scan to the pool from the uploader thread, as an upload would. */
    private void upload(Runnable scan) throws Exception {
        uploader.submit(() -> pool.execute(scan)).get(5, TimeUnit.SECONDS);
    }

    /** A scan whose OCR lasts until the test ends. */
    private Runnable longScan() {
        return () -> {
            try {
                endOfTest.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }

    @Test
    void everyScanIsReadOnAnOcrThread() throws Exception {
        build();
        List<String> readers = Collections.synchronizedList(new ArrayList<>());
        for (int scan = 0; scan < 9; scan++) {
            CountDownLatch read = new CountDownLatch(1);
            upload(() -> {
                readers.add(Thread.currentThread().getName());
                read.countDown();
            });
            assertThat(read.await(5, TimeUnit.SECONDS)).as("scan %d was read", scan).isTrue();
        }
        assertThat(readers).hasSize(9).allSatisfy(name -> assertThat(name).startsWith("ocr-"));
    }

    @Test
    void aFourthScanWaitsForOneOfTheThreeWorkers() throws Exception {
        build();
        for (int scan = 0; scan < 4; scan++) upload(longScan());

        assertThat(pool.getPoolSize()).as("workers").isEqualTo(3);
        assertThat(pool.getQueue()).as("waiting scans").hasSize(1);
    }

    @Test
    void aBurstThatFillsTheWaitingRoomBringsInExtraWorkers() throws Exception {
        build();
        for (int scan = 0; scan < 11; scan++) upload(longScan());

        assertThat(pool.getPoolSize()).as("workers").isEqualTo(5);
        assertThat(pool.getQueue()).as("waiting scans").hasSize(6);
    }

    @Test
    void aScanThatFindsEverythingFullIsReadByItsUploader() throws Exception {
        build();
        for (int scan = 0; scan < 11; scan++) upload(longScan());
        CountDownLatch read = new CountDownLatch(1);
        List<String> reader = Collections.synchronizedList(new ArrayList<>());

        upload(() -> {
            reader.add(Thread.currentThread().getName());
            read.countDown();
        });

        assertThat(read.await(5, TimeUnit.SECONDS)).as("the twelfth scan was read").isTrue();
        assertThat(reader).containsExactly("uploader");
    }
}
