package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricesTest {

    @Test
    void parsesPlainNumbersAndMagnitudes() {
        assertThat(Prices.parse(List.of("010", "08"))).containsExactly(10, 8);
        assertThat(Prices.magnitudes(List.of(-5, 5, 5))).containsExactly(5, 5, 5);
        assertThat(Prices.parse(List.of("42", "7", "-3"))).containsExactly(42, 7, -3);
        assertThat(Prices.parse(List.of())).isEmpty();
        assertThat(Prices.magnitudes(List.of(-5, 3, 0))).containsExactly(5, 3, 0);
    }

    @Test
    void nullEntriesAreSkipped() {
        assertThat(Prices.parse(Arrays.asList("1", null, "2"))).containsExactly(1, 2);
    }

    @Test
    void paddedNumbersAreTrimmedFirst() {
        assertThat(Prices.parse(List.of("  42  ", " 7", "100"))).containsExactly(42, 7, 100);
    }

    @Test
    void blankEntriesAreSkipped() {
        assertThat(Prices.parse(List.of("5", "", "   ", "6"))).containsExactly(5, 6);
    }

    @Test
    void aNonNumberIsRefused() {
        assertThatThrownBy(() -> Prices.parse(List.of("1", "4 2")))
                .isInstanceOf(NumberFormatException.class);
        assertThatThrownBy(() -> Prices.parse(List.of("x", "2")))
                .isInstanceOf(NumberFormatException.class);
    }
}
