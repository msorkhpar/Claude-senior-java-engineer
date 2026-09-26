package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountStrategyTest {

    @Test
    void appliesEachDiscount() {
        assertThat(DiscountStrategy.NONE.applyDiscount(50)).isEqualTo(50.0);
        assertThat(DiscountStrategy.PERCENTAGE_20.applyDiscount(100)).isEqualTo(80.0);
        assertThat(DiscountStrategy.FLAT_5.applyDiscount(30)).isEqualTo(25.0);
        assertThat(DiscountStrategy.BUY_ONE_GET_HALF.applyDiscount(20)).isEqualTo(15.0);
        assertThat(DiscountStrategy.bestDiscount(100)).isEqualTo(DiscountStrategy.BUY_ONE_GET_HALF);
        assertThat(DiscountStrategy.bestDiscount(20)).isEqualTo(DiscountStrategy.FLAT_10);
    }

    @Test
    void aPriceNeverGoesBelowZero() {
        assertThat(DiscountStrategy.FLAT_10.applyDiscount(8)).isEqualTo(0.0);
        assertThat(DiscountStrategy.FLAT_5.applyDiscount(0)).isEqualTo(0.0);
    }

    @Test
    void aNegativeAmountIsRefused() {
        assertThatThrownBy(() -> DiscountStrategy.NONE.applyDiscount(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DiscountStrategy.PERCENTAGE_10.applyDiscount(-0.01)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theResultIsRoundedToCents() {
        assertThat(DiscountStrategy.PERCENTAGE_10.applyDiscount(19.99)).isEqualTo(17.99);
        assertThat(DiscountStrategy.BUY_ONE_GET_HALF.applyDiscount(9.99)).isEqualTo(7.49);
        assertThat(DiscountStrategy.PERCENTAGE_10.applyDiscount(5.02)).isEqualTo(4.52);
    }

    @Test
    void aTieGoesToTheFirstDeclared() {
        assertThat(DiscountStrategy.bestDiscount(40)).isEqualTo(DiscountStrategy.FLAT_10);
        assertThat(DiscountStrategy.bestDiscount(0)).isEqualTo(DiscountStrategy.PERCENTAGE_10);
        assertThat(DiscountStrategy.bestDiscount(5)).isEqualTo(DiscountStrategy.FLAT_5);
    }
}
