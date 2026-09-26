package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ShippingTest {

    @Test
    void chargesTheOuterZones() {
        assertThat(Shipping.fee(2)).isEqualTo(8);
        assertThat(Shipping.fee(3)).isEqualTo(12);
    }

    @Test
    void theInnerZoneKeepsItsOwnFee() {
        assertThat(Shipping.fee(1)).isEqualTo(5);
    }

    @Test
    void anUnknownZoneIsRefused() {
        assertThatThrownBy(() -> Shipping.fee(4)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Shipping.fee(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
