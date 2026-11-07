package practice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RateServiceTest {

    private static final Map<String, BigDecimal> REMOTE = Map.of(
            "GBP/JPY", new BigDecimal("191.42"),
            "CHF/SEK", new BigDecimal("12.07"),
            "AUD/NZD", new BigDecimal("1.0931"));

    private final List<String> apiCalls = new ArrayList<>();

    private final RateService.RateApi api = pair -> {
        apiCalls.add(pair);
        return REMOTE.get(pair);
    };

    /** Keeps what it logs when switched on, drops everything when off. */
    static final class TestLog implements RateService.DebugLog {
        final boolean on;
        final List<String> lines = new ArrayList<>();

        TestLog(boolean on) {
            this.on = on;
        }

        @Override
        public void debug(String message) {
            if (on) lines.add(message);
        }

        @Override
        public void debug(Supplier<String> message) {
            if (on) lines.add(message.get());
        }
    }

    @Test
    void returnsRatesAndLogsEachLookupWhenDebugIsOn() {
        TestLog log = new TestLog(true);
        RateService service = new RateService(api, log);

        assertThat(service.rate("GBP/JPY")).isEqualByComparingTo("191.42");
        assertThat(service.rate("CHF/SEK")).isEqualByComparingTo("12.07");
        assertThat(service.rate("GBP/JPY")).isEqualByComparingTo("191.42");

        assertThat(log.lines).hasSize(3);
        assertThat(log.lines.get(0)).contains("GBP/JPY", "191.42");
        assertThat(log.lines.get(1)).contains("CHF/SEK", "12.07");
        assertThat(log.lines.get(2)).contains("CHF/SEK", "GBP/JPY");   // the snapshot lists the cache
    }

    @Test
    void aPairAlreadyInTheCacheCostsNoApiCall() {
        RateService service = new RateService(api, new TestLog(false));

        for (int i = 0; i < 4; i++) {
            service.rate("AUD/NZD");
            service.rate("CHF/SEK");
        }

        assertThat(apiCalls).containsExactly("AUD/NZD", "CHF/SEK");
    }

    @Test
    void withDebugOffNoSnapshotOfTheCacheIsTaken() {
        RateService service = new RateService(api, new TestLog(false));

        for (int i = 0; i < 5; i++) service.rate("GBP/JPY");

        assertThat(service.snapshotsTaken).isZero();
    }
}
