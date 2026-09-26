package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    void eachCallReachesTheOverloadItsArgumentsMatch() {
        assertThat(calculator.add(5, 10)).isEqualTo(15);
        assertThat(calculator.add(5, 10, 15)).isEqualTo(30);
        assertThat(calculator.add(2.0, 3.0)).isCloseTo(5.0, within(1e-9));
        assertThat(calculator.kind("hi")).isEqualTo("String");
        assertThat(calculator.kind(List.of())).isEqualTo("Object");
    }

    @Test
    void aStringSeenAsAnObjectUsesTheObjectOverload() {
        Object greeting = "hi";
        assertThat(calculator.kind(greeting)).isEqualTo("Object");
    }

    @Test
    void anIntArgumentBoxesToTheIntegerOverload() {
        assertThat(calculator.kind(7)).isEqualTo("Integer");
    }

    @Test
    void doublesAreAddedWithoutTruncation() {
        assertThat(calculator.add(5.5, 10.5)).isCloseTo(16.0, within(1e-9));
        assertThat(calculator.add(0.1, 0.2)).isCloseTo(0.3, within(1e-9));
    }
}
