package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LazyTest {

    @Test
    void createsTheValueOnceAndKeepsIt() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Object> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            return new Object();
        });
        Object first = lazy.get();
        Object second = lazy.get();
        assertThat(first).isNotNull();
        assertThat(second).isSameAs(first);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void nothingIsCreatedBeforeTheFirstGet() {
        AtomicInteger calls = new AtomicInteger();
        Lazy<String> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            return "settings";
        });
        assertThat(calls.get()).isZero();
        assertThat(lazy.get()).isEqualTo("settings");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void concurrentCallersShareOneValue() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        Lazy<Object> lazy = new Lazy<>(() -> {
            calls.incrementAndGet();
            try {
                Thread.sleep(200); // a slow construction, like loading settings
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return new Object();
        });
        int threads = 16;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Future<Object>> results = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return lazy.get();
                }));
            }
            start.countDown();
            Set<Object> seen = ConcurrentHashMap.newKeySet();
            for (Future<Object> result : results) {
                seen.add(result.get(10, TimeUnit.SECONDS));
            }
            assertThat(calls.get()).isEqualTo(1);
            assertThat(seen).hasSize(1);
        } finally {
            pool.shutdownNow();
        }
    }
}
