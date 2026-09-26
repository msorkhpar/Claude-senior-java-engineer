package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadSafeCounterTest {

    @Test
    void countsIncrements() {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        assertThat(counter.getCount()).isZero();
        counter.incrementCount();
        counter.incrementCount();
        counter.incrementCount();
        assertThat(counter.getCount()).isEqualTo(3);
    }

    @Test
    void noIncrementIsLostAcrossThreads() throws InterruptedException {
        ThreadSafeCounter counter = new ThreadSafeCounter();
        int threads = 8;
        int each = 100_000;
        CountDownLatch start = new CountDownLatch(1);
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            Thread worker = new Thread(() -> {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                for (int i = 0; i < each; i++) {
                    counter.incrementCount();
                }
            });
            workers.add(worker);
            worker.start();
        }
        start.countDown();
        for (Thread worker : workers) {
            worker.join();
        }
        assertThat(counter.getCount()).isEqualTo(threads * each);
    }
}
