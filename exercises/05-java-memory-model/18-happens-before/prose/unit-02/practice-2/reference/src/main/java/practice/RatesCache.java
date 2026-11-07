package practice;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Exchange rates for the checkout service. A loader thread reads them from storage once, at
 * startup, and calls onLoaded; request threads call rate() from the first second on.
 */
public class RatesCache {

    private final long timeoutMillis;
    private final CountDownLatch loaded = new CountDownLatch(1);
    private Map<String, BigDecimal> rates = Map.of();

    /** timeoutMillis: how long a request may wait for the rates before it gives up. */
    public RatesCache(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    /** Called once, on the loader's thread, with the rates it read. */
    public void onLoaded(Map<String, BigDecimal> fresh) {
        rates = Map.copyOf(fresh);
        loaded.countDown();
    }

    /** The rate of one currency against the euro; called on request threads. */
    public BigDecimal rate(String currency) throws InterruptedException, TimeoutException {
        awaitLoaded();
        BigDecimal rate = rates.get(currency);
        if (rate == null) throw new NoSuchElementException("no rate for " + currency);
        return rate;
    }

    /** Returns once the rates are loaded, or throws TimeoutException after timeoutMillis. */
    void awaitLoaded() throws InterruptedException, TimeoutException {
        if (!loaded.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            throw new TimeoutException("rates not loaded within " + timeoutMillis + " ms");
        }
    }
}
