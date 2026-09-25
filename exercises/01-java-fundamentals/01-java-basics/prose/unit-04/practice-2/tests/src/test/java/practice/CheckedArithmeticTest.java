package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CheckedArithmeticTest {

    @Test
    void computesTheResult() {
        assertThat(CheckedArithmetic.multiplyAdd(2, 3, 4)).isEqualTo(10);
        assertThat(CheckedArithmetic.multiplyAdd(-2, 3, 1)).isEqualTo(-5);
        assertThat(CheckedArithmetic.multiplyAdd(0, Integer.MAX_VALUE, 7)).isEqualTo(7);
    }

    @Test
    void anOverflowingProductIsRefused() {
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> CheckedArithmetic.multiplyAdd(65_536, 65_536, 0));
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> CheckedArithmetic.multiplyAdd(Integer.MIN_VALUE, -1, 0));
    }

    @Test
    void anOverflowingSumIsRefused() {
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> CheckedArithmetic.multiplyAdd(Integer.MAX_VALUE, 1, 1));
        assertThatExceptionOfType(ArithmeticException.class).isThrownBy(() -> CheckedArithmetic.multiplyAdd(Integer.MIN_VALUE, 1, -1));
    }

    @Test
    void resultsAtTheLimitsAreFine() {
        assertThat(CheckedArithmetic.multiplyAdd(Integer.MAX_VALUE, 1, 0)).isEqualTo(Integer.MAX_VALUE);
        assertThat(CheckedArithmetic.multiplyAdd(-1, Integer.MAX_VALUE, -1)).isEqualTo(Integer.MIN_VALUE);
    }
}
