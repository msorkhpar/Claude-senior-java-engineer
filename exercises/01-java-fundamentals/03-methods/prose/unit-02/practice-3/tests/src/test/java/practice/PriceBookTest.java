package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PriceBookTest {

    /** A quote service that answers 9.99 for anything and records what it was asked. */
    private static final class CountingQuotes implements PriceBook.QuoteService {
        final List<String> asked = new ArrayList<>();

        @Override
        public BigDecimal quote(String sku) {
            asked.add(sku);
            return new BigDecimal("9.99");
        }
    }

    private static PriceBook shop(CountingQuotes quotes) {
        PriceBook book = new PriceBook(quotes);
        book.list("KB-104", new BigDecimal("49.00"));
        book.list("MS-220", new BigDecimal("18.50"));
        book.list("HD-900", null);                  // listed, not priced yet
        return book;
    }

    @Test
    void aProductWithAPriceOnFileHasThatPrice() {
        PriceBook book = shop(new CountingQuotes());

        assertThat(book.price("KB-104")).contains(new BigDecimal("49.00"));
        assertThat(book.priceOrQuote("MS-220")).isEqualTo(new BigDecimal("18.50"));
    }

    @Test
    void aProductTheShopDoesNotListHasNoPriceAndGetsAQuote() {
        CountingQuotes quotes = new CountingQuotes();
        PriceBook book = shop(quotes);

        assertThat(book.price("CB-001")).isEmpty();
        assertThat(book.priceOrQuote("CB-001")).isEqualTo(new BigDecimal("9.99"));
        assertThat(quotes.asked).containsExactly("CB-001");
    }

    @Test
    void aProductListedWithoutAPriceHasNoPriceAndGetsAQuote() {
        CountingQuotes quotes = new CountingQuotes();
        PriceBook book = shop(quotes);

        assertThat(book.price("HD-900")).isEmpty();
        assertThat(book.priceOrQuote("HD-900")).isEqualTo(new BigDecimal("9.99"));
        assertThat(quotes.asked).containsExactly("HD-900");
    }

    @Test
    void aPriceOnFileCostsNoQuote() {
        CountingQuotes quotes = new CountingQuotes();
        PriceBook book = shop(quotes);

        book.priceOrQuote("KB-104");
        book.priceOrQuote("MS-220");

        assertThat(quotes.asked).isEmpty();
    }
}
