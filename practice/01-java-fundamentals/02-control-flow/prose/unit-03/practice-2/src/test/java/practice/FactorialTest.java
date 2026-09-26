package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class FactorialTest {

    @Test
    void computesTheFactorial() {
        assertThat(Factorial.of(0)).isEqualTo(1L);
        assertThat(Factorial.of(1)).isEqualTo(1L);
        assertThat(Factorial.of(5)).isEqualTo(120L);
        assertThat(Factorial.of(10)).isEqualTo(3628800L);
    }

    @Test
    void aNegativeNumberIsRefused() {
        assertThatThrownBy(() -> Factorial.of(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anOverflowIsRefused() {
        assertThat(Factorial.of(20)).isEqualTo(2432902008176640000L);
        assertThatThrownBy(() -> Factorial.of(21)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Factorial.of(25)).isInstanceOf(ArithmeticException.class);
    }
}
