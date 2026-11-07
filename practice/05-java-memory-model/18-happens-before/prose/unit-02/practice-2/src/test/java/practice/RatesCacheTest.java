package practice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RatesCacheTest {

    private static final Map<String, BigDecimal> RATES = Map.of(
            "CHF", new BigDecimal("0.9412"),
            "NOK", new BigDecimal("11.6705"),
            "SEK", new BigDecimal("11.2280"));

    /** A call running on a daemon thread of its own. */
    static final class Call<T> {
        final FutureTask<T> task;
        final Thread thread;

        Call(Callable<T> call) {
            task = new FutureTask<>(call);
            thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

        T get() throws Exception {
            return task.get(5, TimeUnit.SECONDS);
        }

        /** Waits, at most five seconds, until the call has returned or is parked waiting. */
        boolean finishedBeforeParking() {
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < end) {
                if (task.isDone()) return true;
                if (LockSupport.getBlocker(thread) != null) return false;
                Thread.onSpinWait();
            }
            return task.isDone();
        }
    }

    /** Rates whose copying stalls, like a map read from slow storage, until it is released. */
    static final class StallingRates extends AbstractMap<String, BigDecimal> {
        final CountDownLatch copying = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);

        @Override
        public Set<Entry<String, BigDecimal>> entrySet() {
            copying.countDown();
            try {
                release.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return RATES.entrySet();
        }

        @Override
        public BigDecimal get(Object key) {
            return RATES.get(key);
        }
    }

    @Test
    void answersWithRatesLoadedOnAnotherThread() throws Exception {
        RatesCache cache = new RatesCache(5_000);
        new Call<>(() -> { cache.onLoaded(RATES); return null; }).get();

        assertThat(new Call<>(() -> cache.rate("CHF")).get()).isEqualByComparingTo("0.9412");
        assertThat(new Call<>(() -> cache.rate("SEK")).get()).isEqualByComparingTo("11.2280");
    }

    @Test
    void theRatesStayWhenTheLoaderReusesItsMap() throws Exception {
        RatesCache cache = new RatesCache(5_000);
        Map<String, BigDecimal> scratch = new HashMap<>(RATES);
        new Call<>(() -> { cache.onLoaded(scratch); scratch.clear(); return null; }).get();

        assertThat(new Call<>(() -> cache.rate("NOK")).get()).isEqualByComparingTo("11.6705");
    }

    @Test
    void aRequestThatArrivesBeforeTheRatesWaitsForThem() throws Exception {
        RatesCache cache = new RatesCache(10_000);
        Call<BigDecimal> request = new Call<>(() -> cache.rate("NOK"));

        assertThat(request.finishedBeforeParking())
                .as("the request returned before any rates were loaded").isFalse();
        new Call<>(() -> { cache.onLoaded(RATES); return null; }).get();

        assertThat(request.get()).isEqualByComparingTo("11.6705");
    }

    @Test
    void aRequestGivesUpWithATimeoutWhenTheRatesNeverArrive() {
        RatesCache cache = new RatesCache(50);

        assertThatThrownBy(() -> new Call<>(() -> cache.rate("CHF")).get())
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    void aRequestNeverAnswersFromRatesThatAreStillBeingStored() throws Exception {
        RatesCache cache = new RatesCache(10_000);
        StallingRates stalling = new StallingRates();
        try {
            Call<Void> loader = new Call<>(() -> { cache.onLoaded(stalling); return null; });
            long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (!stalling.copying.await(10, TimeUnit.MILLISECONDS) && !loader.task.isDone()
                    && System.nanoTime() < end) {
                // wait until the loader is copying the rates, or has finished without copying
            }

            boolean stillStoring = stalling.copying.getCount() == 0 && !loader.task.isDone();

            Call<BigDecimal> request = new Call<>(() -> cache.rate("SEK"));
            boolean early = request.finishedBeforeParking();
            stalling.release.countDown();

            if (stillStoring) {
                assertThat(early).as("the request answered while the rates were still being stored").isFalse();
            }
            assertThat(request.get()).isEqualByComparingTo("11.2280");
        } finally {
            stalling.release.countDown();
        }
    }
}
