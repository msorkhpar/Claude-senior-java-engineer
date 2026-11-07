package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class FxDeskTest {

    /** A book that remembers which thread made each call. */
    static final class RecordingBook implements FxDesk.Book {
        final Map<String, Long> rates = new HashMap<>();
        final List<String> threads = Collections.synchronizedList(new ArrayList<>());
        final CountDownLatch applied = new CountDownLatch(1);

        @Override
        public void apply(String pair, long micros) {
            threads.add("apply on " + Thread.currentThread().getName());
            rates.put(pair, micros);
            applied.countDown();
        }

        @Override
        public Long rate(String pair) {
            threads.add("rate on " + Thread.currentThread().getName());
            return rates.get(pair);
        }
    }

    private final ExecutorService desk = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "desk");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stopDesk() {
        desk.shutdownNow();
    }

    /** Completes the quote on an adapter thread of its own, the way market data arrives. */
    private static void arriveFromAdapter(CompletableFuture<Long> quote, long micros) throws InterruptedException {
        Thread adapter = new Thread(() -> quote.complete(micros), "adapter-7");
        adapter.setDaemon(true);
        adapter.start();
        adapter.join(5_000);
        assertThat(adapter.isAlive()).as("the adapter finished completing the quote").isFalse();
    }

    @Test
    void aQuoteThatArrivesLaterEndsUpInTheBook() throws Exception {
        RecordingBook book = new RecordingBook();
        FxDesk fx = new FxDesk(desk, book);
        CompletableFuture<Long> quote = new CompletableFuture<>();

        fx.onQuote("GBP/JPY", quote);
        arriveFromAdapter(quote, 191_420_000L);

        assertThat(book.applied.await(5, TimeUnit.SECONDS)).as("the quote was applied").isTrue();
        assertThat(fx.rate("GBP/JPY").get(5, TimeUnit.SECONDS)).isEqualTo(191_420_000L);
    }

    @Test
    void aQuoteAlreadyInHandEndsUpInTheBook() throws Exception {
        RecordingBook book = new RecordingBook();
        FxDesk fx = new FxDesk(desk, book);

        fx.onQuote("USD/CHF", CompletableFuture.completedFuture(882_310L));

        assertThat(book.applied.await(5, TimeUnit.SECONDS)).as("the quote was applied").isTrue();
        assertThat(fx.rate("USD/CHF").get(5, TimeUnit.SECONDS)).isEqualTo(882_310L);
    }

    @Test
    void aPairNeverQuotedHasNoRate() throws Exception {
        FxDesk fx = new FxDesk(desk, new RecordingBook());

        assertThat(fx.rate("NZD/SEK").get(5, TimeUnit.SECONDS)).isNull();
    }

    @Test
    void aQuoteThatArrivesLaterIsAppliedOnTheDeskThread() throws Exception {
        RecordingBook book = new RecordingBook();
        FxDesk fx = new FxDesk(desk, book);
        CompletableFuture<Long> quote = new CompletableFuture<>();

        fx.onQuote("AUD/CAD", quote);
        arriveFromAdapter(quote, 903_550L);

        assertThat(book.applied.await(5, TimeUnit.SECONDS)).as("the quote was applied").isTrue();
        assertThat(book.threads).containsExactly("apply on desk");
    }

    @Test
    void aQuoteAlreadyInHandIsAppliedOnTheDeskThread() throws Exception {
        RecordingBook book = new RecordingBook();
        FxDesk fx = new FxDesk(desk, book);

        fx.onQuote("EUR/NOK", CompletableFuture.completedFuture(11_684_200L));

        assertThat(book.applied.await(5, TimeUnit.SECONDS)).as("the quote was applied").isTrue();
        assertThat(book.threads).containsExactly("apply on desk");
    }

    @Test
    void theRateIsReadOnTheDeskThread() throws Exception {
        RecordingBook book = new RecordingBook();
        FxDesk fx = new FxDesk(desk, book);

        fx.rate("CAD/JPY").get(5, TimeUnit.SECONDS);

        assertThat(book.threads).containsExactly("rate on desk");
    }
}
