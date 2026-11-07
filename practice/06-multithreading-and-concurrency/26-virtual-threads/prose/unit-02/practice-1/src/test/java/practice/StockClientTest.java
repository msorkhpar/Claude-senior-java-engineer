package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class StockClientTest {

    /** A fake inventory API: counts calls in progress, can hold them at a gate, fails SKUs starting with "gone-". */
    static final class FakeInventory implements StockClient.Inventory {
        final AtomicInteger inProgress = new AtomicInteger();
        final AtomicInteger peak = new AtomicInteger();
        final Semaphore entered = new Semaphore(0);
        final CountDownLatch gate;

        FakeInventory(boolean held) {
            gate = new CountDownLatch(held ? 1 : 0);
        }

        @Override
        public int stock(String sku) throws Exception {
            peak.accumulateAndGet(inProgress.incrementAndGet(), Math::max);
            try {
                entered.release();
                if (!gate.await(15, TimeUnit.SECONDS)) throw new IllegalStateException("gate never opened");
                if (sku.startsWith("gone-")) throw new IllegalArgumentException("404 " + sku);
                return sku.length() * 7;
            } finally {
                inProgress.decrementAndGet();
            }
        }

        /** Waits until n more calls have entered; false after the timeout. */
        boolean awaitEntered(int n, long millis) throws InterruptedException {
            return entered.tryAcquire(n, millis, TimeUnit.MILLISECONDS);
        }
    }

    private static final ExecutorService CALLERS = Executors.newVirtualThreadPerTaskExecutor();

    @Test
    void looksUpEverySku() throws Exception {
        FakeInventory api = new FakeInventory(false);
        StockClient client = new StockClient(api, 2, Map.of("bolt-m6", 410));
        Future<Map<String, Integer>> levels = CALLERS.submit(
                () -> client.stockAll(List.of("washer-9", "bolt-m6", "hinge-22b", "nut-4")));
        assertThat(levels.get(10, TimeUnit.SECONDS)).containsExactly(
                Map.entry("washer-9", 56), Map.entry("bolt-m6", 410),
                Map.entry("hinge-22b", 63), Map.entry("nut-4", 35));
    }

    @Test
    void theApiNeverSeesMoreCallsThanItAccepts() throws Exception {
        FakeInventory api = new FakeInventory(true);
        StockClient client = new StockClient(api, 3, Map.of());
        List<String> skus = List.of("a-1", "a-2", "a-3", "a-4", "a-5", "a-6", "a-7", "a-8", "a-9", "a-10");
        Future<Map<String, Integer>> levels = CALLERS.submit(() -> client.stockAll(skus));
        try {
            assertThat(api.awaitEntered(3, 10_000)).as("three calls reach the API").isTrue();
            assertThat(api.awaitEntered(1, 1_000)).as("a fourth call while three are in progress").isFalse();
        } finally {
            api.gate.countDown();
        }
        assertThat(levels.get(10, TimeUnit.SECONDS)).hasSize(10);
        assertThat(api.peak.get()).isEqualTo(3);
    }

    @Test
    void failedCallsDoNotUseUpTheLimit() throws Exception {
        FakeInventory api = new FakeInventory(false);
        StockClient client = new StockClient(api, 2, Map.of());
        for (String sku : List.of("gone-1", "gone-2", "gone-3")) {
            Future<Integer> failed = CALLERS.submit(() -> client.stock(sku));
            assertThatThrownBy(() -> failed.get(10, TimeUnit.SECONDS))
                    .isInstanceOf(ExecutionException.class).hasRootCauseInstanceOf(IllegalArgumentException.class);
        }
        Future<Integer> next = CALLERS.submit(() -> client.stock("spring-3"));
        assertThat(next.get(5, TimeUnit.SECONDS)).as("a call after three failures").isEqualTo(56);
    }

    @Test
    void knownSkusAreAnsweredWhileTheApiIsBusy() throws Exception {
        FakeInventory api = new FakeInventory(true);
        StockClient client = new StockClient(api, 2, Map.of("cog-5", 12));
        Future<Map<String, Integer>> slow = CALLERS.submit(() -> client.stockAll(List.of("x-1", "x-2", "x-3")));
        try {
            assertThat(api.awaitEntered(2, 10_000)).as("two calls reach the API").isTrue();
            Future<Map<String, Integer>> known = CALLERS.submit(() -> client.stockAll(List.of("cog-5")));
            assertThat(known.get(3, TimeUnit.SECONDS)).containsExactly(Map.entry("cog-5", 12));
        } catch (TimeoutException e) {
            throw new AssertionError("a known SKU waited for the busy API", e);
        } finally {
            api.gate.countDown();
        }
        assertThat(slow.get(10, TimeUnit.SECONDS)).hasSize(3);
    }

    @Test
    void anInterruptedCallerDoesNotRaiseTheLimit() throws Exception {
        FakeInventory api = new FakeInventory(true);
        StockClient client = new StockClient(api, 1, Map.of());
        Future<Integer> first = CALLERS.submit(() -> client.stock("p-1"));
        try {
            assertThat(api.awaitEntered(1, 10_000)).as("the first call reaches the API").isTrue();
            CountDownLatch done = new CountDownLatch(1);
            Thread cancelled = Thread.ofVirtual().unstarted(() -> {
                try {
                    client.stock("p-2");
                } catch (Exception expected) {
                    // interrupted while waiting for its turn
                } finally {
                    done.countDown();
                }
            });
            cancelled.interrupt();                         // its request is cancelled before it gets a turn
            cancelled.start();
            assertThat(done.await(10, TimeUnit.SECONDS)).as("the interrupted caller gives up").isTrue();
            Future<Integer> third = CALLERS.submit(() -> client.stock("p-3"));
            assertThat(api.awaitEntered(1, 1_000)).as("a second call while the first is in progress").isFalse();
            api.gate.countDown();
            assertThat(third.get(10, TimeUnit.SECONDS)).isEqualTo(21);
        } finally {
            api.gate.countDown();
        }
        assertThat(first.get(10, TimeUnit.SECONDS)).isEqualTo(21);
        assertThat(api.peak.get()).isEqualTo(1);
    }
}
