package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class QuoteBoardTest {

    private static Callable<QuoteBoard.Quote> offers(String supplier, long cents) {
        return () -> new QuoteBoard.Quote(supplier, cents);
    }

    @Test
    void asksEverySupplierAndReturnsTheCheapestOffer() throws InterruptedException {
        Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
        suppliers.put("brightparts", offers("brightparts", 4_390));
        suppliers.put("kestrel", () -> null);                          // doesn't stock it
        suppliers.put("lowfield", offers("lowfield", 3_975));
        suppliers.put("ostrava-metal", offers("ostrava-metal", 4_120));

        assertThat(QuoteBoard.cheapest(suppliers)).contains(new QuoteBoard.Quote("lowfield", 3_975));
    }

    @Test
    void aFailedSupplierFailsTheCheckWithWhatItThrew() {
        IOException outage = new IOException("HTTP 503 from quotes.example.com");
        Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
        suppliers.put("brightparts", offers("brightparts", 4_390));
        suppliers.put("kestrel", () -> { throw outage; });

        Throwable thrown = catchThrowable(() -> QuoteBoard.cheapest(suppliers));

        assertThat(thrown).isInstanceOf(QuoteBoard.QuoteFailed.class);
        assertThat(thrown.getCause()).isSameAs(outage);
    }

    @Test
    void aFailedSupplierIsNotLeftOutOfTheCheck() {
        Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
        suppliers.put("kestrel", () -> { throw new IllegalStateException("price list not loaded"); });
        suppliers.put("lowfield", offers("lowfield", 3_975));
        suppliers.put("ostrava-metal", offers("ostrava-metal", 4_120));

        Throwable thrown = catchThrowable(() -> QuoteBoard.cheapest(suppliers));

        assertThat(thrown).isInstanceOf(QuoteBoard.QuoteFailed.class);
    }

    @Test
    void theSuppliersAreAskedAtTheSameTime() throws InterruptedException {
        CountDownLatch lowfieldAsked = new CountDownLatch(1);
        Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
        suppliers.put("brightparts", () -> {
            // brightparts answers only once lowfield has been asked too
            if (!lowfieldAsked.await(3, TimeUnit.SECONDS)) throw new TimeoutException("asked one at a time");
            return new QuoteBoard.Quote("brightparts", 4_390);
        });
        suppliers.put("lowfield", () -> {
            lowfieldAsked.countDown();
            return new QuoteBoard.Quote("lowfield", 3_975);
        });

        assertThat(QuoteBoard.cheapest(suppliers)).contains(new QuoteBoard.Quote("lowfield", 3_975));
    }

    @Test
    void eachSupplierIsAskedOnAThreadNamedForIt() throws InterruptedException {
        List<String> askedOn = new CopyOnWriteArrayList<>();
        Map<String, Callable<QuoteBoard.Quote>> suppliers = new LinkedHashMap<>();
        suppliers.put("brightparts", () -> {
            askedOn.add(Thread.currentThread().getName());
            return new QuoteBoard.Quote("brightparts", 4_390);
        });
        suppliers.put("ostrava-metal", () -> {
            askedOn.add(Thread.currentThread().getName());
            return new QuoteBoard.Quote("ostrava-metal", 4_120);
        });

        QuoteBoard.cheapest(suppliers);

        assertThat(askedOn).containsExactlyInAnyOrder("quote-brightparts", "quote-ostrava-metal");
    }
}
