package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RateCacheTest {

    private final List<String> serviceCalls = new CopyOnWriteArrayList<>();

    private int ratesService(String zone) {
        serviceCalls.add(zone);
        return switch (zone) {
            case "ZONE-NORDIC" -> 1890;
            case "ZONE-IBERIA" -> 1475;
            default -> 2250;
        };
    }

    /** Runs the call on a daemon thread; the future completes with what it returns. */
    private static <T> CompletableFuture<T> onItsOwnThread(Supplier<T> call) {
        CompletableFuture<T> result = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                result.complete(call.get());
            } catch (Throwable e) {
                result.completeExceptionally(e);
            }
        });
        t.setDaemon(true);
        t.start();
        return result;
    }

    @Test
    void ratesSetByTheTariffJobAreServedFromTheCache() {
        RateCache cache = new RateCache(this::ratesService);
        cache.replace("ZONE-BALTIC", 1320);
        cache.replace("ZONE-ALPINE", 2105);

        assertThat(cache.rateFor("ZONE-BALTIC")).isEqualTo(1320);
        assertThat(cache.rateFor("ZONE-ALPINE")).isEqualTo(2105);
        cache.replace("ZONE-BALTIC", 1355);
        assertThat(cache.rateFor("ZONE-BALTIC")).isEqualTo(1355);
        assertThat(serviceCalls).isEmpty();
    }

    @Test
    void aZoneNotYetCachedIsLoadedOnceAndThenServedFromTheCache() throws Exception {
        RateCache cache = new RateCache(this::ratesService);

        CompletableFuture<Integer> first = onItsOwnThread(() -> cache.rateFor("ZONE-NORDIC"));
        assertThat(first.get(5, TimeUnit.SECONDS)).isEqualTo(1890);
        CompletableFuture<Integer> second = onItsOwnThread(() -> cache.rateFor("ZONE-NORDIC"));
        assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(1890);
        assertThat(serviceCalls).containsExactly("ZONE-NORDIC");
    }

    @Test
    void aCheckoutReadsARateWhileAnotherCheckoutIsReading() throws Exception {
        RateCache cache = new RateCache(this::ratesService);
        cache.replace("ZONE-IBERIA", 1475);
        CountDownLatch reading = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        Thread otherCheckout = new Thread(() -> {
            cache.rw.readLock().lock();                         // a long read in progress
            try {
                reading.countDown();
                done.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            } finally {
                cache.rw.readLock().unlock();
            }
        });
        otherCheckout.setDaemon(true);
        otherCheckout.start();
        assertThat(reading.await(5, TimeUnit.SECONDS)).isTrue();
        try {
            CompletableFuture<Integer> rate = onItsOwnThread(() -> cache.rateFor("ZONE-IBERIA"));
            assertThat(rate.get(5, TimeUnit.SECONDS)).isEqualTo(1475);
        } finally {
            done.countDown();
        }
    }
}
