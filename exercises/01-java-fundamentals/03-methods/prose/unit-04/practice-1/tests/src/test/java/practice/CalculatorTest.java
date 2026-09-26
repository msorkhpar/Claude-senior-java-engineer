package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CalculatorTest {

    @Test
    void addsIntsByCount() {
        assertThat(Calculator.add(5, 10)).isEqualTo(15);
        assertThat(Calculator.add(5, 10, 15)).isEqualTo(30);
        assertThat(Calculator.add(-1, 1)).isZero();
    }

    @Test
    void doublesKeepTheirFractions() {
        assertThat(Calculator.add(5.5, 10.25)).isEqualTo(15.75);
        assertThat(Calculator.add(0.5, 0.5)).isEqualTo(1.0);
    }

    @Test
    void theOrderOfParametersMatters() {
        assertThat(Calculator.concat("x", 7)).isEqualTo("x7");
        assertThat(Calculator.concat(7, "x")).isEqualTo("7x");
    }
}
