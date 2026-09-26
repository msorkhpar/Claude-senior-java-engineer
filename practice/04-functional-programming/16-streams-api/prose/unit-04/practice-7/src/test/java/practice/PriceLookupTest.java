package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceLookupTest {

    private static final Map<String, Integer> PRICES = Map.of("tea", 3, "cake", 5, "soup", 7);

    @Test
    void looksUpEveryCode() {
        assertThat(PriceLookup.pricesOf(List.of("tea", "cake"), PRICES)).containsExactly(3, 5);
        assertThat(PriceLookup.pricesOf(List.of("soup", "cake", "soup"), PRICES)).containsExactly(7, 5, 7);
        assertThat(PriceLookup.pricesOf(List.of(), PRICES)).isEmpty();
    }

    @Test
    void resultIsUnmodifiable() {
        List<Integer> result = PriceLookup.pricesOf(List.of("tea", "cake"), PRICES);
        assertThatThrownBy(() -> result.add(9)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> result.set(0, 9)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void unknownCodesBecomeNull() {
        assertThat(PriceLookup.pricesOf(List.of("tea", "pie", "cake"), PRICES))
                .isEqualTo(Arrays.asList(3, null, 5));
    }

    @Test
    void codesMatchByValue() {
        List<String> codes = List.of(new StringBuilder("te").append('a').toString(), String.valueOf(new char[] {'s', 'o', 'u', 'p'}));
        assertThat(PriceLookup.pricesOf(codes, PRICES)).containsExactly(3, 7);
    }
}
