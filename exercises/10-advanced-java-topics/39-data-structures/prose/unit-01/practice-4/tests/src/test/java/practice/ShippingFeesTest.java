package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShippingFeesTest {

    private static ShippingFees table() {
        return new ShippingFees()
                .tier(1_500, 790)
                .tier(100, 320)
                .tier(5_000, 1_450)
                .tier(500, 480)
                .tier(20_000, 3_900);
    }

    @Test
    void chargesTheTierAParcelFallsInto() {
        ShippingFees fees = table();

        assertThat(fees.feeFor(250)).isEqualTo(320);
        assertThat(fees.feeFor(1_499)).isEqualTo(480);
        assertThat(fees.feeFor(3_200)).isEqualTo(790);
        assertThat(fees.feeFor(64_000)).isEqualTo(3_900);
        assertThat(fees.tierStartsBetween(400, 6_000)).containsExactly(500, 1_500, 5_000);
    }

    @Test
    void aParcelExactlyAtATiersStartPaysThatTier() {
        ShippingFees fees = table();

        assertThat(fees.feeFor(1_500)).isEqualTo(790);
        assertThat(fees.feeFor(5_000)).isEqualTo(1_450);
        assertThat(fees.feeFor(100)).isEqualTo(320);
    }

    @Test
    void aParcelLighterThanEveryTierIsRefused() {
        ShippingFees fees = table();

        assertThatThrownBy(() -> fees.feeFor(99)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> fees.feeFor(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aRangeIncludesATierStartingAtItsUpperEnd() {
        ShippingFees fees = table();

        assertThat(fees.tierStartsBetween(500, 5_000)).containsExactly(500, 1_500, 5_000);
        assertThat(fees.tierStartsBetween(20_000, 20_000)).containsExactly(20_000);
    }
}
