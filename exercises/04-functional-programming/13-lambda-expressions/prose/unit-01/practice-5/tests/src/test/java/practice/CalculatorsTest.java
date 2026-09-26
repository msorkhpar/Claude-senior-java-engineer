package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculatorsTest {

    @Test
    void buildsTheFourOperations() {
        assertThat(Calculators.of('+').calculate(5, 3)).isEqualTo(8);
        assertThat(Calculators.of('*').calculate(5, 3)).isEqualTo(15);
        assertThat(Calculators.of('-').calculate(5, 3)).isEqualTo(2);
        assertThat(Calculators.of('-').calculate(3, 5)).isEqualTo(-2);
        assertThat(Calculators.of('/').calculate(7, 2)).isEqualTo(3);
    }

    @Test
    void defaultAndStaticMethodsStillWork() {
        Calculator addition = Calculators.of('+');
        assertThat(addition.square(5)).isEqualTo(25);
        assertThat(addition.calculate(10, 20)).isEqualTo(Calculator.add(10, 20));
    }

    @Test
    void unknownOperatorIsRejectedAtOnce() {
        assertThatThrownBy(() -> Calculators.of('%'))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown operator: %");
        assertThatThrownBy(() -> Calculators.of('x'))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown operator: x");
        assertThatThrownBy(() -> Calculators.of(':'))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("unknown operator: :");
    }

    @Test
    void divisionByZeroThrows() {
        Calculator division = Calculators.of('/');
        assertThatThrownBy(() -> division.calculate(1, 0))
                .isExactlyInstanceOf(ArithmeticException.class);
    }

    @Test
    void divisionRoundsTowardZero() {
        Calculator division = Calculators.of('/');
        assertThat(division.calculate(-7, 2)).isEqualTo(-3);
        assertThat(division.calculate(7, -2)).isEqualTo(-3);
    }
}
