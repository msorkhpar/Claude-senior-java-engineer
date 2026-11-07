package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RateTableTest {

    private static final Map<String, Long> MORNING = rates(
            "CHF", 1_068_300L, "USD", 921_400L, "SEK", 87_950L, "NOK", 85_610L);
    private static final Map<String, Long> NOON = rates(
            "CHF", 1_071_050L, "USD", 919_870L, "SEK", 88_020L, "NOK", 85_390L);

    private final ExecutorService checkout = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    /** Released by each test; a load held by the store waits for it, at most 10 seconds. */
    private final CountDownLatch release = new CountDownLatch(1);

    @AfterEach
    void stopTheCheckoutThreads() {
        release.countDown();
        checkout.shutdownNow();
    }

    private static Map<String, Long> rates(Object... pairs) {
        Map<String, Long> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], (Long) pairs[i + 1]);
        return m;
    }

    /** A store that counts its loads and serves whatever {@code current} holds. */
    private static final class CountingStore implements RateTable.RateStore {
        final AtomicInteger loads = new AtomicInteger();
        volatile Map<String, Long> current = MORNING;

        @Override
        public void load(BiConsumer<String, Long> sink) {
            loads.incrementAndGet();
            current.forEach(sink);
        }
    }

    /**
     * A store whose first load sends two rates, tells {@code midLoad}, and waits for
     * {@code release} before it sends the rest. Every later load runs straight through.
     */
    private final class HeldStore implements RateTable.RateStore {
        final CountDownLatch midLoad = new CountDownLatch(1);
        private final AtomicInteger loads = new AtomicInteger();

        @Override
        public void load(BiConsumer<String, Long> sink) {
            boolean first = loads.incrementAndGet() == 1;
            int sent = 0;
            for (Map.Entry<String, Long> e : MORNING.entrySet()) {
                if (first && sent == 2) {
                    midLoad.countDown();
                    try {
                        release.await(10, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                sink.accept(e.getKey(), e.getValue());
                sent++;
            }
        }
    }

    private <T> T within5s(Future<T> f) throws Exception {
        return f.get(5, TimeUnit.SECONDS);
    }

    @Test
    void answersFromOneLoadUntilRefreshed() throws Exception {
        CountingStore store = new CountingStore();
        RateTable table = new RateTable(store);

        assertThat(within5s(checkout.submit(() -> table.rate("SEK")))).isEqualTo(87_950L);
        assertThat(within5s(checkout.submit(() -> table.rate("USD")))).isEqualTo(921_400L);
        assertThat(within5s(checkout.submit(table::rates))).isEqualTo(MORNING);
        assertThat(store.loads.get()).isEqualTo(1);

        store.current = NOON;
        table.refresh();

        assertThat(within5s(checkout.submit(() -> table.rate("CHF")))).isEqualTo(1_071_050L);
        assertThat(within5s(checkout.submit(table::rates))).isEqualTo(NOON);
        assertThat(store.loads.get()).isEqualTo(2);
    }

    @Test
    void aLookupDoesNotWaitForALoadAnotherThreadStarted() throws Exception {
        HeldStore store = new HeldStore();
        RateTable table = new RateTable(store);
        Future<Long> first = checkout.submit(() -> table.rate("USD"));
        assertThat(store.midLoad.await(5, TimeUnit.SECONDS)).as("the first lookup started a load").isTrue();

        assertThat(within5s(checkout.submit(() -> table.rate("CHF")))).isEqualTo(1_068_300L);

        release.countDown();
        assertThat(within5s(first)).isEqualTo(921_400L);
    }

    @Test
    void aLookupDuringAnotherThreadsLoadSeesEveryRate() throws Exception {
        HeldStore store = new HeldStore();
        RateTable table = new RateTable(store);
        Future<Long> first = checkout.submit(() -> table.rate("USD"));
        assertThat(store.midLoad.await(5, TimeUnit.SECONDS)).as("the first lookup started a load").isTrue();

        Future<Long> nok = checkout.submit(() -> table.rate("NOK"));
        Future<Map<String, Long>> all = checkout.submit(table::rates);
        assertThat(within5s(nok)).isEqualTo(85_610L);
        assertThat(within5s(all)).isEqualTo(MORNING);

        release.countDown();
        assertThat(within5s(first)).isEqualTo(921_400L);
    }

    @Test
    void aCallerCannotChangeTheRatesOtherCallersSee() throws Exception {
        RateTable table = new RateTable(new CountingStore());
        Map<String, Long> forTheReport = within5s(checkout.submit(table::rates));

        assertThatCode(() -> {
            try {
                forTheReport.put("XAU", 2_215_000_000L);
                forTheReport.remove("SEK");
            } catch (UnsupportedOperationException refused) {
                // refusing the change is one way to keep the others' rates intact
            }
        }).doesNotThrowAnyException();

        assertThat(within5s(checkout.submit(table::rates))).isEqualTo(MORNING);
        assertThat(within5s(checkout.submit(() -> table.rate("SEK")))).isEqualTo(87_950L);
    }
}
