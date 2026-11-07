package practice;

import org.junit.jupiter.api.Test;
import practice.QuoteMetrics.Metered;
import practice.QuoteMetrics.Quote;
import practice.QuoteMetrics.QuoteSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteMetricsTest {

    /** A remote quote service: every call to it is one request over the network. */
    static final class RemoteQuotes implements QuoteSource {
        int requests;

        private static long price(String symbol) {
            return 100L * symbol.length() + symbol.charAt(0);
        }

        @Override
        public Quote quote(String symbol) {
            requests++;
            return new Quote(symbol, price(symbol));
        }

        @Override
        public Map<String, Quote> quoteAll(List<String> symbols) {
            requests++;                                  // one request for the whole list
            Map<String, Quote> quotes = new LinkedHashMap<>();
            for (String symbol : symbols) quotes.put(symbol, new Quote(symbol, price(symbol)));
            return quotes;
        }
    }

    private static final List<String> PAGE = List.of("ASML", "SAP", "NOVO-B", "TTE", "SIE", "MC");

    @Test
    void aPageGetsAQuoteForEverySymbolInOrder() {
        Metered metered = new Metered(new RemoteQuotes());

        Map<String, Quote> quotes = metered.quoteAll(PAGE);

        assertThat(quotes.keySet()).containsExactlyElementsOf(PAGE);
        assertThat(quotes.get("SAP")).isEqualTo(new Quote("SAP", 383));
    }

    @Test
    void aPageCostsTheRemoteServiceOneRequest() {
        RemoteQuotes remote = new RemoteQuotes();
        Metered metered = new Metered(remote);

        metered.quoteAll(PAGE);

        assertThat(remote.requests).isEqualTo(1);
    }

    @Test
    void aOneSymbolPageIsCounted() {
        Metered metered = new Metered(new RemoteQuotes());

        metered.quoteAll(List.of("RHM"));

        assertThat(metered.symbolsQuoted()).isEqualTo(1);
    }

    @Test
    void theDashboardCountsEverySymbolOnEveryPage() {
        Metered metered = new Metered(new RemoteQuotes());

        metered.quoteAll(PAGE);
        metered.quote("ADS");
        metered.quoteAll(List.of("BAYN", "DTE"));

        assertThat(metered.symbolsQuoted()).isEqualTo(9);
    }
}
