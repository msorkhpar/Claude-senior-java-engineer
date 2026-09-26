package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DividerTest {

    @Test
    void dividesTwoNumbers() {
        assertThat(Divider.divide(10, 2)).isEqualTo(5.0);
        assertThat(Divider.divide(15, 3)).isEqualTo(5.0);
        assertThat(Divider.divide(1, 3)).isCloseTo(0.3333333, within(1e-6));
        assertThat(Divider.divide(-9, 3)).isEqualTo(-3.0);
    }

    @Test
    void aZeroDenominatorIsRefused() {
        assertThatThrownBy(() -> Divider.divide(10, 0))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot divide by zero");
        assertThatThrownBy(() -> Divider.divide(10, -0.0)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void zeroOverZeroIsRefusedToo() {
        assertThatThrownBy(() -> Divider.divide(0, 0)).isInstanceOf(ArithmeticException.class);
    }
}
