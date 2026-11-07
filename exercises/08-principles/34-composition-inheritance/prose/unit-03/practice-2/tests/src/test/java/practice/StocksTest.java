package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class StocksTest {

    /** A stock that answers from a fixed table and records what it was asked. */
    static final class Recording implements Stocks.Stock {
        final List<String> calls = new ArrayList<>();
        RuntimeException failure;

        @Override
        public int level(String sku) {
            calls.add("level " + sku);
            if (failure != null) throw failure;
            return sku.length() * 3;
        }

        @Override
        public void set(String sku, int units) {
            calls.add("set " + sku + " " + units);
            if (failure != null) throw failure;
        }
    }

    @Test
    void passesEachCallToTheStockItWrapsAndCountsIt() {
        Stocks.MeteredStock warehouse = new Stocks.MeteredStock(new Stocks.MapStock());
        warehouse.set("teal-mug", 14);
        warehouse.set("oak-tray", 0);
        assertThat(warehouse.level("teal-mug")).isEqualTo(14);
        assertThat(warehouse.level("oak-tray")).isZero();
        assertThat(warehouse.level("never-stocked")).isZero();
        assertThat(warehouse.succeeded()).isEqualTo(5);
        assertThat(warehouse.failed()).isZero();

        Recording fake = new Recording();
        Stocks.MeteredStock metered = new Stocks.MeteredStock(fake);
        assertThat(metered.level("linen-apron")).isEqualTo(33);
        metered.set("felt-coaster", 250);
        assertThat(fake.calls).containsExactly("level linen-apron", "set felt-coaster 250");
        assertThat(metered.succeeded()).isEqualTo(2);
    }

    @Test
    void countsCallsFromManyThreads() throws Exception {
        Stocks.MeteredStock metered = new Stocks.MeteredStock(new Stocks.Stock() {
            public int level(String sku) { return 7; }
            public void set(String sku, int units) { }
        });
        ExecutorService pool = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        try {
            List<Future<?>> done = new ArrayList<>();
            for (int t = 0; t < 4; t++) {
                done.add(pool.submit(() -> {
                    for (int i = 0; i < 50_000; i++) metered.level("pine-crate");
                }));
            }
            for (Future<?> f : done) f.get(30, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
        assertThat(metered.succeeded()).isEqualTo(200_000);
    }

    @Test
    void aCallThatFailsIsCountedAsAFailureOnly() {
        Stocks.MeteredStock warehouse = new Stocks.MeteredStock(new Stocks.MapStock());
        catchThrowable(() -> warehouse.set("teal-mug", -2));
        assertThat(warehouse.succeeded()).isZero();
        assertThat(warehouse.failed()).isEqualTo(1);

        Recording fake = new Recording();
        fake.failure = new IllegalStateException("store offline");
        Stocks.MeteredStock metered = new Stocks.MeteredStock(fake);
        catchThrowable(() -> metered.level("teal-mug"));
        catchThrowable(() -> metered.set("teal-mug", 5));
        assertThat(metered.succeeded()).isZero();
        assertThat(metered.failed()).isEqualTo(2);
    }

    @Test
    void theCallerGetsTheExceptionTheStockThrew() {
        Recording fake = new Recording();
        RuntimeException offline = new UnsupportedOperationException("read-only replica");
        fake.failure = offline;
        Stocks.MeteredStock metered = new Stocks.MeteredStock(fake);
        assertThat(catchThrowable(() -> metered.level("brass-hook"))).isSameAs(offline);
        assertThat(catchThrowable(() -> metered.set("brass-hook", 9))).isSameAs(offline);

        Stocks.MeteredStock warehouse = new Stocks.MeteredStock(new Stocks.MapStock());
        assertThat(catchThrowable(() -> warehouse.set("brass-hook", -1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("brass-hook");
    }
}
