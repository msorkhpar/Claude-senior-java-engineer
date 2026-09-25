package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RoundingTest {

    @Test
    void roundsToTheNearestWholeNumber() {
        assertThat(Rounding.roundToWhole(19.99)).isEqualTo(20);
        assertThat(Rounding.roundToWhole(19.4)).isEqualTo(19);
        assertThat(Rounding.roundToWhole(3.14)).isEqualTo(3);
        assertThat(Rounding.roundToWhole(2.5)).isEqualTo(3);
    }

    @Test
    void roundsNegativeAmountsToo() {
        assertThat(Rounding.roundToWhole(-19.99)).isEqualTo(-20);
        assertThat(Rounding.roundToWhole(-2.5)).isEqualTo(-2);
        assertThat(Rounding.roundToWhole(-0.4)).isEqualTo(0);
    }

    @Test
    void refusesAResultBeyondInt() {
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> Rounding.roundToWhole(3.0e9));
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> Rounding.roundToWhole(-3.0e9));
    }
}
