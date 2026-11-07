package practice;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PriceBookTest {

    private PriceBook stocked() {
        PriceBook book = new PriceBook();
        book.put("tea-earl-grey", 450L);
        book.put("tea-sencha", 820L);
        book.put("mug-blue", 1299L);
        return book;
    }

    @Test
    void adjustmentsAndTheYearlyIncreaseChangeThePrices() {
        PriceBook book = stocked();

        book.merge("tea-sencha", -20L, Long::sum);
        book.merge("kettle-steel", 3500L, Long::sum);
        book.replaceAll((sku, cents) -> cents + 100);

        assertThat(book).containsExactlyInAnyOrderEntriesOf(Map.of(
                "tea-earl-grey", 550L, "tea-sencha", 900L, "mug-blue", 1399L, "kettle-steel", 3600L));
    }

    @Test
    void anAdjustmentIsInTheTrail() {
        PriceBook book = stocked();

        book.merge("mug-blue", 150L, Long::sum);
        book.merge("kettle-steel", 3500L, Long::sum);

        assertThat(book.trail()).endsWith("mug-blue: 1299 -> 1449", "kettle-steel: none -> 3500");
    }

    @Test
    void theYearlyIncreaseIsInTheTrailOncePerSku() {
        PriceBook book = stocked();

        book.replaceAll((sku, cents) -> cents * 2);

        assertThat(book.trail()).hasSize(6);
        assertThat(book.trail().subList(3, 6)).containsExactlyInAnyOrder(
                "tea-earl-grey: 450 -> 900", "tea-sencha: 820 -> 1640", "mug-blue: 1299 -> 2598");
    }

    @Test
    void anAdjustmentThatDiscontinuesASkuRemovesIt() {
        PriceBook book = stocked();

        Long result = book.merge("tea-sencha", 0L, (old, ignored) -> null);

        assertThat(result).isNull();
        assertThat(book).doesNotContainKey("tea-sencha").hasSize(2);
        assertThat(book.trail()).endsWith("tea-sencha: 820 -> removed");
    }
}
