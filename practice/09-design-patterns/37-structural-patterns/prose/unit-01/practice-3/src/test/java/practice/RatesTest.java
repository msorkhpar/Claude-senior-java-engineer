package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class RatesTest {

    /** A stand-in for the legacy client: it answers when the test says so, on a thread of its own. */
    static final class FakeClient implements Rates.RateClient {
        final Map<String, Rates.RateListener> waiting = new ConcurrentHashMap<>();

        @Override
        public void lookup(String pair, Rates.RateListener listener) {
            if (!pair.matches("[A-Z]{3}/[A-Z]{3}")) throw new IllegalArgumentException("not a currency pair: " + pair);
            waiting.put(pair, listener);
        }

        void answer(String pair, String rate) throws InterruptedException {
            onClientThread(() -> waiting.remove(pair).onRate(pair, new BigDecimal(rate)));
        }

        void fail(String pair, String reason) throws InterruptedException {
            onClientThread(() -> waiting.remove(pair).onFailure(pair, reason));
        }

        private static void onClientThread(Runnable callback) throws InterruptedException {
            Thread t = new Thread(callback, "fx-io");
            t.setDaemon(true);
            t.start();
            t.join(5_000);
            assertThat(t.isAlive()).as("the client's thread returned from the callback").isFalse();
        }
    }

    private final FakeClient client = new FakeClient();
    private final Rates rates = new Rates(client);
    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "caller");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopCaller() {
        caller.shutdownNow();
    }

    /** Calls rate() on another thread, so an implementation that blocks can't hang the test. */
    private CompletableFuture<BigDecimal> rate(String pair) throws Exception {
        Future<CompletableFuture<BigDecimal>> call = caller.submit(() -> rates.rate(pair));
        return call.get(5, TimeUnit.SECONDS);
    }

    @Test
    void theClientsRateCompletesTheFuture() throws Exception {
        CompletableFuture<BigDecimal> gbpJpy = rate("GBP/JPY");
        assertThat(gbpJpy).isNotDone();

        client.answer("GBP/JPY", "191.42");

        assertThat(gbpJpy).succeedsWithin(Duration.ofSeconds(5)).isEqualTo(new BigDecimal("191.42"));
    }

    @Test
    void eachLookupGetsItsOwnAnswer() throws Exception {
        CompletableFuture<BigDecimal> audNzd = rate("AUD/NZD");
        CompletableFuture<BigDecimal> usdCad = rate("USD/CAD");

        client.answer("USD/CAD", "1.3607");
        client.answer("AUD/NZD", "1.0918");

        assertThat(audNzd).succeedsWithin(Duration.ofSeconds(5)).isEqualTo(new BigDecimal("1.0918"));
        assertThat(usdCad).succeedsWithin(Duration.ofSeconds(5)).isEqualTo(new BigDecimal("1.3607"));
    }

    @Test
    void aFailureTheClientReportsFailsTheFuture() throws Exception {
        CompletableFuture<BigDecimal> chfSek = rate("CHF/SEK");

        client.fail("CHF/SEK", "market closed");

        assertThat(chfSek).failsWithin(Duration.ofSeconds(5))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .havingCause()
                .isInstanceOf(Rates.RateUnavailableException.class)
                .withMessageContaining("market closed");
    }

    @Test
    void aPairTheClientRefusesFailsTheFutureInsteadOfThrowing() throws Exception {
        CompletableFuture<BigDecimal> refused = rate("sek-nok");

        assertThat(refused).failsWithin(Duration.ofSeconds(5))
                .withThrowableOfType(java.util.concurrent.ExecutionException.class)
                .havingCause()
                .isInstanceOf(IllegalArgumentException.class);
    }
}
