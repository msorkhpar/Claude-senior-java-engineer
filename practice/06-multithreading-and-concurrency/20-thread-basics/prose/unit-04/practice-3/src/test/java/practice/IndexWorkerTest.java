package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;

class IndexWorkerTest {

    private static final Map<String, String> SYNONYMS = Map.of("colour", "color", "aeroplane", "airplane");

    private final BlockingQueue<String> indexed = new LinkedBlockingQueue<>();
    private final List<IndexWorker> workers = new ArrayList<>();
    private final List<Thread> started = new ArrayList<>();
    private final AtomicBoolean loaderMayFinish = new AtomicBoolean();

    @AfterEach
    void stopEverything() throws InterruptedException {
        loaderMayFinish.set(true);
        for (IndexWorker w : workers) w.stop();
        for (Thread t : started) t.interrupt();
        for (Thread t : started) t.join(5_000);
    }

    private IndexWorker worker(IndexWorker.Loader loader) {
        IndexWorker w = new IndexWorker(loader, indexed::add);
        workers.add(w);
        return w;
    }

    /** Calls start() on a daemon thread; the future says whether the table was loaded when start() returned. */
    private FutureTask<Boolean> startInBackground(IndexWorker w) {
        FutureTask<Boolean> task = new FutureTask<>(() -> {
            w.start();
            return w.loaded();
        });
        Thread t = new Thread(task, "search-boot");
        t.setDaemon(true);
        started.add(t);
        t.start();
        return task;
    }

    /** Gives start() two seconds to return early, as a wrong one would. */
    private static void letItReturnEarlyIfItWill(FutureTask<Boolean> start) throws Exception {
        try {
            start.get(2, TimeUnit.SECONDS);
        } catch (TimeoutException stillStarting) {
            // a right start() is still waiting for the table
        }
    }

    @Test
    void indexesTermsWithTheirSynonymsOnceStarted() throws Exception {
        IndexWorker w = worker(() -> SYNONYMS);

        assertThat(startInBackground(w).get(5, TimeUnit.SECONDS)).isNotNull();
        w.submit("colour");
        w.submit("tram");

        assertThat(indexed.poll(5, TimeUnit.SECONDS)).isEqualTo("color");
        assertThat(indexed.poll(5, TimeUnit.SECONDS)).isEqualTo("tram");
    }

    @Test
    void startReturnsOnlyOnceTheTableIsLoadedWhenLoadingWaitsForALock() throws Exception {
        ReentrantLock sharedConfig = new ReentrantLock();
        CountDownLatch loading = new CountDownLatch(1);
        FutureTask<Boolean> start;
        sharedConfig.lock();                               // another thread is reloading the config
        try {
            start = startInBackground(worker(() -> {
                loading.countDown();
                sharedConfig.lockInterruptibly();
                try {
                    return SYNONYMS;
                } finally {
                    sharedConfig.unlock();
                }
            }));
            assertThat(loading.await(5, TimeUnit.SECONDS)).isTrue();
            letItReturnEarlyIfItWill(start);
        } finally {
            sharedConfig.unlock();
        }

        assertThat(start.get(5, TimeUnit.SECONDS)).as("loaded when start() returned").isTrue();
    }

    @Test
    void startReturnsOnlyOnceTheTableIsLoadedWhenLoadingComputesForAWhile() throws Exception {
        CountDownLatch loading = new CountDownLatch(1);
        FutureTask<Boolean> start = startInBackground(worker(() -> {
            loading.countDown();
            while (!loaderMayFinish.get()) Thread.onSpinWait();     // building the table
            return SYNONYMS;
        }));
        assertThat(loading.await(5, TimeUnit.SECONDS)).isTrue();
        letItReturnEarlyIfItWill(start);

        loaderMayFinish.set(true);

        assertThat(start.get(5, TimeUnit.SECONDS)).as("loaded when start() returned").isTrue();
    }
}
