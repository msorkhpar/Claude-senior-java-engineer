package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceLookupTest {

    private static final Map<String, Integer> TABLE = Map.of("flour-1kg", 189, "oats-500g", 245, "yeast", 99);

    @Test
    void aBasketOfKnownCodesGetsItsPricesInOrder() {
        List<String> codes = List.of("yeast", "flour-1kg", "yeast", "oats-500g");

        assertThat(PriceLookup.pricesOf(codes, TABLE)).containsExactly(99, 189, 99, 245);
    }

    @Test
    void aCodeWithoutAPriceLeavesAGapInItsPlace() {
        List<String> codes = List.of("oats-500g", "rye-2kg", "yeast");

        List<Integer> prices = PriceLookup.pricesOf(codes, TABLE);

        assertThat(prices).containsExactly(245, null, 99);
    }

    @Test
    void nobodyCanChangeTheListTheyAreGiven() {
        List<Integer> prices = PriceLookup.pricesOf(List.of("flour-1kg", "yeast"), TABLE);

        assertThatThrownBy(() -> prices.add(1)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> prices.set(0, 1)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> prices.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(prices).containsExactly(189, 99);
    }

    @Test
    void labelsAreTheCurrentCodesAsAStringArray() {
        Object labels = PriceLookup.labelsFor(List.of("yeast", "old-spelt", "oats-500g"));

        assertThat(labels).isInstanceOf(String[].class);
        assertThat((String[]) labels).containsExactly("yeast", "oats-500g");
    }
}
