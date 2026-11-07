package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PriceServiceTest {

    private static final ThreadFactory DAEMONS = r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    };

    private static final Map<String, Long> CACHED = Map.of("sku-4471", 3_150L, "sku-0093", 72_900L, "sku-8120", 499L);

    /** The pool the product pages share: one thread, so a thread kept by a stuck call is easy to see. */
    private final ExecutorService pool = Executors.newSingleThreadExecutor(DAEMONS);
    private final ExecutorService page = Executors.newSingleThreadExecutor(DAEMONS);
    private final CountDownLatch supplierWakesUp = new CountDownLatch(1);
    private final CountDownLatch supplierInterrupted = new CountDownLatch(1);

    @AfterEach
    void cleanUp() {
        supplierWakesUp.countDown();
        pool.shutdownNow();
        page.shutdownNow();
    }

    /** A supplier that stops answering for sku-0093 and answers at once for anything else. */
    private final PriceService.Supplier stuckOn0093 = sku -> {
        if (!sku.equals("sku-0093")) return 100L;
        try {
            supplierWakesUp.await();
        } catch (InterruptedException e) {
            supplierInterrupted.countDown();
            throw e;
        }
        return 70_000L;
    };

    /** Asks for a price the way a product page does, on a thread of its own, so a call that never returns can't hang the test. */
    private long pageAsks(PriceService prices, String sku) throws Exception {
        Future<Long> shown = page.submit(() -> prices.priceOf(sku));
        return shown.get(5, TimeUnit.SECONDS);
    }

    @Test
    void aSupplierThatAnswersInTimeSetsThePrice() throws Exception {
        Map<String, Long> live = Map.of("sku-4471", 3_099L, "sku-8120", 549L);
        PriceService prices = new PriceService(pool, live::get, CACHED, 5_000);

        assertThat(pageAsks(prices, "sku-4471")).isEqualTo(3_099L);
        assertThat(pageAsks(prices, "sku-8120")).isEqualTo(549L);
    }

    @Test
    void aSupplierThatStopsAnsweringGetsTheCachedPrice() throws Exception {
        PriceService prices = new PriceService(pool, stuckOn0093, CACHED, 100);

        assertThat(pageAsks(prices, "sku-0093")).isEqualTo(72_900L);
    }

    @Test
    void aStuckSupplierCallGivesItsThreadBackToThePool() throws Exception {
        PriceService prices = new PriceService(pool, stuckOn0093, CACHED, 100);

        pageAsks(prices, "sku-0093");

        assertThat(supplierInterrupted.await(5, TimeUnit.SECONDS)).isTrue();
        Future<String> nextPage = pool.submit(() -> "served");
        assertThat(nextPage.get(5, TimeUnit.SECONDS)).isEqualTo("served");
    }

    @Test
    void aSupplierThatFailsGetsTheCachedPrice() throws Exception {
        PriceService.Supplier down = sku -> {
            throw new ConnectException("pricing.example.com: connection refused");
        };
        PriceService prices = new PriceService(pool, down, CACHED, 5_000);

        assertThat(pageAsks(prices, "sku-8120")).isEqualTo(499L);
    }
}
