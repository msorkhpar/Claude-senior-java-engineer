package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteFormatterTest {

    private static final Map<String, Long> CENTS = Map.of(
            "LAMP-3", 4_999L, "BULB-60W", 350L, "CABLE-2M", 1_275L,
            "DESK-OAK", 31_900L, "CHAIR-K", 12_450L);

    /** The price of "BULB-60W" waits until the test lets it go: a slow pricing service. */
    private final CountDownLatch slowLookupStarted = new CountDownLatch(1);
    private final CountDownLatch letSlowLookupFinish = new CountDownLatch(1);

    private final QuoteFormatter formatter = new QuoteFormatter(sku -> {
        if (sku.equals("BULB-60W")) {
            slowLookupStarted.countDown();
            try {
                letSlowLookupFinish.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return CENTS.get(sku);
    });

    private final ExecutorService requests = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stop() {
        letSlowLookupFinish.countDown();
        requests.shutdownNow();
    }

    private static String line(String label, String amount) {
        return String.format("%-12s %12s\n", label, amount);
    }

    /** Starts a quote that stops inside the slow lookup, half done, and waits until it is there. */
    private Future<String> halfDoneQuote() throws InterruptedException {
        Future<String> first = requests.submit(() -> formatter.render(List.of("LAMP-3", "BULB-60W", "CABLE-2M")));
        assertThat(slowLookupStarted.await(5, TimeUnit.SECONDS)).as("the first quote reached its slow lookup").isTrue();
        return first;
    }

    @Test
    void rendersOneLinePerSkuAndTheTotal() throws Exception {
        String lines = requests.submit(() -> formatter.render(List.of("DESK-OAK", "CHAIR-K"))).get(5, TimeUnit.SECONDS);

        assertThat(lines).isEqualTo(line("DESK-OAK", "319.00") + line("CHAIR-K", "124.50") + line("TOTAL", "443.50"));
    }

    @Test
    void quotesOneAfterAnotherDoNotCarryAnythingOver() throws Exception {
        requests.submit(() -> formatter.quote("Jane Doe", List.of("DESK-OAK", "CHAIR-K"))).get(5, TimeUnit.SECONDS);
        String second = requests.submit(() -> formatter.quote("John Roe", List.of("CABLE-2M"))).get(5, TimeUnit.SECONDS);

        assertThat(second).isEqualTo("Quote for John Roe\n" + line("CABLE-2M", "12.75") + line("TOTAL", "12.75"));
    }

    @Test
    void aQuoteRenderedWhileAnotherIsHalfDoneKeepsItsOwnLines() throws Exception {
        Future<String> first = halfDoneQuote();
        String second = requests.submit(() -> formatter.render(List.of("DESK-OAK", "CHAIR-K"))).get(5, TimeUnit.SECONDS);
        letSlowLookupFinish.countDown();
        String firstLines = first.get(5, TimeUnit.SECONDS);

        assertThat(second).startsWith(line("DESK-OAK", "319.00") + line("CHAIR-K", "124.50"));
        assertThat(firstLines).startsWith(line("LAMP-3", "49.99") + line("BULB-60W", "3.50") + line("CABLE-2M", "12.75"));
    }

    @Test
    void aQuoteRenderedWhileAnotherIsHalfDoneKeepsItsOwnTotal() throws Exception {
        Future<String> first = halfDoneQuote();
        requests.submit(() -> formatter.render(List.of("DESK-OAK", "CHAIR-K"))).get(5, TimeUnit.SECONDS);
        letSlowLookupFinish.countDown();
        String firstLines = first.get(5, TimeUnit.SECONDS);

        assertThat(firstLines).endsWith(line("TOTAL", "66.24"));
    }

    @Test
    void aQuoteDoesNotWaitForAnotherStuckOnAPrice() throws Exception {
        Future<String> first = halfDoneQuote();
        Future<String> second = requests.submit(() -> formatter.render(List.of("CHAIR-K")));

        try {
            assertThat(second.get(5, TimeUnit.SECONDS)).isEqualTo(line("CHAIR-K", "124.50") + line("TOTAL", "124.50"));
        } catch (TimeoutException e) {
            throw new AssertionError("the second quote was still waiting 5 s after the first one stopped at a slow price", e);
        } finally {
            letSlowLookupFinish.countDown();
        }
        first.get(5, TimeUnit.SECONDS);
    }
}
