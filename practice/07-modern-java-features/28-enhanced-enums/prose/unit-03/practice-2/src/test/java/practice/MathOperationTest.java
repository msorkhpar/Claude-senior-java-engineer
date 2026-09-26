package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MathOperationTest {

    @Test
    void eachConstantAppliesItsOwnOperation() {
        assertThat(MathOperation.ADD.apply(2, 3)).isEqualTo(5.0);
        assertThat(MathOperation.SUBTRACT.apply(2, 3)).isEqualTo(-1.0);
        assertThat(MathOperation.MULTIPLY.apply(4, 2.5)).isEqualTo(10.0);
        assertThat(MathOperation.DIVIDE.apply(7, 2)).isEqualTo(3.5);
        assertThat(MathOperation.MODULUS.apply(7, 3)).isEqualTo(1.0);
        assertThat(MathOperation.fromSymbol("*")).contains(MathOperation.MULTIPLY);
        assertThat(MathOperation.fromSymbol("^")).isEmpty();
        assertThat(MathOperation.qualifiedName(MathOperation.Precedence.HIGH)).isEqualTo("Precedence.HIGH");
    }

    @Test
    void aZeroDivisorIsRefused() {
        assertThatThrownBy(() -> MathOperation.DIVIDE.apply(7, 0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MathOperation.MODULUS.apply(7, 0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MathOperation.DIVIDE.apply(7, -0.0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MathOperation.DIVIDE.apply(0, 0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> MathOperation.MODULUS.apply(7, -0.0)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void findsASymbolBuiltAtRuntime() {
        String symbol = String.valueOf((char) ('0' - 5));
        assertThat(symbol).isEqualTo("+");
        assertThat(MathOperation.fromSymbol(symbol)).contains(MathOperation.ADD);
        assertThat(MathOperation.fromSymbol(new StringBuilder().append('%').toString())).contains(MathOperation.MODULUS);
    }

    @Test
    void namesAConstantWithABody() {
        assertThat(MathOperation.qualifiedName(MathOperation.ADD)).isEqualTo("MathOperation.ADD");
        assertThat(MathOperation.qualifiedName(MathOperation.MODULUS)).isEqualTo("MathOperation.MODULUS");
    }
}
