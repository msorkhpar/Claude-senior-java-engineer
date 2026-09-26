package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PricingTest {

    @Test
    void pricesAnOrder() {
        assertThat(Pricing.calculateTax(100, 0.2)).isCloseTo(120.0, within(1e-9));
        assertThat(Pricing.calculateDiscount(100, 0.2)).isCloseTo(80.0, within(1e-9));
        assertThat(Pricing.onlinePrice(10.0, 5)).isCloseTo(10.0, within(1e-9));
        assertThat(Pricing.onlinePrice(10.0, 60)).isCloseTo(9.0, within(1e-9));
        assertThat(Pricing.inStorePrice(10.0, 60)).isCloseTo(9.0, within(1e-9));
        assertThat(Pricing.inStorePrice(10.0, 150)).isCloseTo(8.0, within(1e-9));
    }

    @Test
    void bothChannelsAgreeAtEveryTierBoundary() {
        int[] quantities = {9, 10, 49, 50, 99, 100};
        double[] expected = {10.0, 9.5, 9.5, 9.0, 9.0, 8.0};
        for (int i = 0; i < quantities.length; i++) {
            assertThat(Pricing.applyBulkDiscount(10.0, quantities[i])).as("rule at %d", quantities[i]).isCloseTo(expected[i], within(1e-9));
            assertThat(Pricing.onlinePrice(10.0, quantities[i])).as("online at %d", quantities[i]).isCloseTo(expected[i], within(1e-9));
            assertThat(Pricing.inStorePrice(10.0, quantities[i])).as("in store at %d", quantities[i]).isCloseTo(expected[i], within(1e-9));
        }
    }

    @Test
    void ratesOfZeroAndOneAreValidAndMeanDifferentThings() {
        assertThat(Pricing.calculateTax(50, 0.0)).isCloseTo(50.0, within(1e-9));
        assertThat(Pricing.calculateTax(50, 1.0)).isCloseTo(100.0, within(1e-9));
        assertThat(Pricing.calculateDiscount(50, 0.0)).isCloseTo(50.0, within(1e-9));
        assertThat(Pricing.calculateDiscount(50, 1.0)).isCloseTo(0.0, within(1e-9));
    }

    @Test
    void eachRuleRefusesInItsOwnWords() {
        assertThatThrownBy(() -> Pricing.calculateTax(100, 1.5))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Tax rate must be between 0 and 1");
        assertThatThrownBy(() -> Pricing.calculateDiscount(100, -0.1))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Discount rate must be between 0 and 1");
        assertThatThrownBy(() -> Pricing.calculateTax(-1, 0.1))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must not be negative");
        assertThatThrownBy(() -> Pricing.calculateDiscount(-1, 0.1))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Amount must not be negative");
    }

    @Test
    void theBulkRuleIsNotRounded() throws Exception {
        assertThat(Pricing.applyBulkDiscount(0.99, 10)).isEqualTo(0.99 * 0.95);
        assertThat(Pricing.onlinePrice(0.33, 100)).isEqualTo(0.33 * 0.80);
    }

    @Test
    void theChannelsAgreeBeyondTheLastTier() throws Exception {
        for (int quantity : new int[] {101, 500, 1000}) {
            assertThat(Pricing.inStorePrice(10.0, quantity)).isEqualTo(8.0);
            assertThat(Pricing.onlinePrice(10.0, quantity)).isEqualTo(8.0);
        }
    }

    @Test
    void theRateBoundsAreExact() throws Exception {
        assertThatThrownBy(() -> Pricing.calculateTax(100, -0.0000001)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tax rate must be between 0 and 1");
        assertThatThrownBy(() -> Pricing.calculateDiscount(100, 1.0000001)).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Discount rate must be between 0 and 1");
    }
}
