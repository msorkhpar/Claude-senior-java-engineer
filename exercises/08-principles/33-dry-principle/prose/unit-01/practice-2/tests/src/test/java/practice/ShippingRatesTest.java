package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShippingRatesTest {

    @Test
    void standardRatesPriceAParcel() {
        ShippingRates rates = ShippingRates.standard();
        assertThat(rates.shippingCost(10)).isCloseTo(1.5, within(1e-9));
        assertThat(rates.deliveryEstimate(10)).isCloseTo(6.5, within(1e-9));
        assertThat(rates.shippingCost(0)).isCloseTo(0.0, within(1e-9));
        assertThat(rates.deliveryEstimate(0)).isCloseTo(5.0, within(1e-9));
        assertThat(rates.shippingCost(0.01)).isCloseTo(0.0015, within(1e-12));
        assertThat(rates.deliveryEstimate(0.01)).isCloseTo(5.0015, within(1e-12));
    }

    @Test
    void aNewRateReachesBothPrices() {
        ShippingRates rates = new ShippingRates(0.40, 2.0);
        assertThat(rates.shippingCost(10)).isCloseTo(4.0, within(1e-9));
        assertThat(rates.deliveryEstimate(10)).isCloseTo(6.0, within(1e-9));
    }

    @Test
    void aNegativeWeightIsRefusedByBoth() {
        ShippingRates rates = ShippingRates.standard();
        assertThatThrownBy(() -> rates.shippingCost(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> rates.deliveryEstimate(-1)).isInstanceOf(IllegalArgumentException.class);
        for (double weight : new double[] {-0.5, -0.001}) {
            assertThatThrownBy(() -> rates.shippingCost(weight)).as("shippingCost(%s)", weight).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> rates.deliveryEstimate(weight)).as("deliveryEstimate(%s)", weight).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
