package practice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/** Quotes for the pricing page, and the decorator that feeds the dashboard's "symbols quoted" panel. */
public class QuoteMetrics {

    public record Quote(String symbol, long priceCents) {}

    /** Where the pricing page gets its quotes. */
    public interface QuoteSource {

        Quote quote(String symbol);

        /**
         * Quotes several symbols, in the order given. This default asks for them one at a time;
         * a remote source overrides it to fetch them all in one request.
         */
        default Map<String, Quote> quoteAll(List<String> symbols) {
            Map<String, Quote> quotes = new LinkedHashMap<>();
            for (String symbol : symbols) quotes.put(symbol, quote(symbol));
            return quotes;
        }
    }

    /** Counts every symbol quoted through it, for the dashboard, and passes each request on. */
    public static class Metered implements QuoteSource {
        private final QuoteSource inner;
        private final LongAdder symbolsQuoted = new LongAdder();

        public Metered(QuoteSource inner) {
            this.inner = inner;
        }

        @Override
        public Quote quote(String symbol) {
            symbolsQuoted.increment();
            return inner.quote(symbol);
        }

        @Override
        public Map<String, Quote> quoteAll(List<String> symbols) {
            symbolsQuoted.increment();
            return inner.quoteAll(symbols);
        }

        /** The number the dashboard shows: symbols quoted through this source so far. */
        public long symbolsQuoted() {
            return symbolsQuoted.sum();
        }
    }
}
