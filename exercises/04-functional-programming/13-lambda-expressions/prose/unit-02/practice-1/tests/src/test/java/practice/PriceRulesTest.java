package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PriceRulesTest {

    @Test
    void appliesDiscount() {
        assertThat(PriceRules.discounted(List.of(100.0, 50.0), 10)).containsExactly(90.0, 45.0);
        assertThat(PriceRules.discounted(List.of(80.0), 0)).containsExactly(80.0);
        assertThat(PriceRules.discounted(List.of(), 10)).isEmpty();
    }

    @Test
    void categorizesPrices() {
        assertThat(PriceRules.categorize(List.of(5.0, 50.0, 500.0, 5000.0)))
                .containsExactly("cheap", "moderate", "expensive", "premium");
    }

    @Test
    void roundsToCents() {
        assertThat(PriceRules.discounted(List.of(19.99), 15)).containsExactly(16.99);
        assertThat(PriceRules.discounted(List.of(10.0), 33)).containsExactly(6.7);
    }

    @Test
    void boundariesMoveUp() {
        assertThat(PriceRules.categorize(List.of(9.99, 10.0, 100.0, 1000.0)))
                .containsExactly("cheap", "moderate", "expensive", "premium");
    }

    @Test
    void roundsToTheNearestCent() {
        assertThat(PriceRules.discounted(List.of(3.33), 10)).containsExactly(3.0);
    }
}
