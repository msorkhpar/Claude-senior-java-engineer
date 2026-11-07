package practice;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuoteServiceTest {

    private static final BigDecimal KETTLE = new BigDecimal("34.90");
    private static final BigDecimal TOASTER = new BigDecimal("52.15");

    @Test
    void quotesFromTheBackendAndCountsTheQuoteWhileItRuns() throws IOException {
        AtomicInteger seenInFlight = new AtomicInteger(-1);
        QuoteService[] service = new QuoteService[1];
        service[0] = new QuoteService(sku -> {
            seenInFlight.set(service[0].inFlight());
            return sku.equals("KT-220") ? KETTLE : TOASTER;
        }, 3);

        assertThat(service[0].quote("KT-220")).isEqualTo(KETTLE);
        assertThat(seenInFlight.get()).isEqualTo(1);
        assertThat(service[0].inFlight()).isZero();
    }

    @Test
    void aSecondQuoteForTheSameSkuComesFromTheCache() throws IOException {
        AtomicInteger calls = new AtomicInteger();
        QuoteService service = new QuoteService(sku -> {
            calls.incrementAndGet();
            return TOASTER;
        }, 3);

        assertThat(service.quote("TS-4")).isEqualTo(TOASTER);
        assertThat(service.quote("TS-4")).isEqualTo(TOASTER);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void refusesAQuoteWhileTheLimitIsInFlight() throws IOException {
        AtomicInteger refused = new AtomicInteger();
        QuoteService[] service = new QuoteService[1];
        service[0] = new QuoteService(sku -> {
            if (sku.equals("KT-220")) {
                try {
                    service[0].quote("TS-4");
                } catch (QuoteService.BusyException e) {
                    refused.incrementAndGet();
                }
            }
            return KETTLE;
        }, 1);

        assertThat(service[0].quote("KT-220")).isEqualTo(KETTLE);
        assertThat(refused.get()).isEqualTo(1);
    }

    @Test
    void aBackendThatFailsWithAnIOExceptionDoesNotKeepCountingTheQuote() {
        QuoteService service = new QuoteService(sku -> {
            throw new IOException("pricing backend: connection reset");
        }, 2);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> service.quote("BL-9")).isInstanceOf(IOException.class);
        }
        assertThat(service.inFlight()).isZero();
    }

    @Test
    void aBackendThatFailsWithAnUncheckedExceptionDoesNotKeepCountingTheQuote() {
        QuoteService service = new QuoteService(sku -> {
            throw new NumberFormatException("For input string: \"12,50\"");
        }, 2);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> service.quote("BL-9")).isInstanceOf(NumberFormatException.class);
        }
        assertThat(service.inFlight()).isZero();
    }

    @Test
    void aQuoteAnsweredFromTheCacheDoesNotKeepCountingIt() throws IOException {
        QuoteService service = new QuoteService(sku -> TOASTER, 5);
        service.quote("TS-4");

        service.quote("TS-4");
        service.quote("TS-4");

        assertThat(service.inFlight()).isZero();
    }

    @Test
    void theBackendsFailureReachesTheCallerUnchanged() {
        IOException reset = new IOException("pricing backend: connection reset");
        QuoteService service = new QuoteService(sku -> {
            throw reset;
        }, 2);

        assertThatThrownBy(() -> service.quote("MX-71")).isSameAs(reset);
    }
}
