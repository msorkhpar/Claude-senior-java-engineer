package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderMathTest {

    @Test
    void computesOrdinaryTotals() {
        assertThat(OrderMath.lineTotal(1999, 3)).isEqualTo(5997);
        assertThat(OrderMath.total(5997, 1000)).isEqualTo(6997);
        assertThat(OrderMath.total()).isZero();
        assertThat(OrderMath.distance(3, 10)).isEqualTo(7);
        assertThat(OrderMath.distance(10, 3)).isEqualTo(7);
    }

    @Test
    void aLineTotalThatOverflowsIsRefused() {
        assertThatThrownBy(() -> OrderMath.lineTotal(65536, 65536)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> OrderMath.lineTotal(1_000_000, 3_000)).isInstanceOf(ArithmeticException.class);
        assertThat(OrderMath.lineTotal(46340, 46340)).isEqualTo(2_147_395_600);
    }

    @Test
    void aSumThatOverflowsIsRefused() {
        assertThatThrownBy(() -> OrderMath.total(Integer.MAX_VALUE, 1, -1)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> OrderMath.total(2_000_000_000, 2_000_000_000)).isInstanceOf(ArithmeticException.class);
        assertThat(OrderMath.total(Integer.MAX_VALUE, -1, 1)).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void theDistanceFromMinValueIsRefused() {
        assertThatThrownBy(() -> OrderMath.distance(Integer.MIN_VALUE, 0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> OrderMath.distance(Integer.MAX_VALUE, -1)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> OrderMath.distance(Integer.MAX_VALUE, -2)).isInstanceOf(ArithmeticException.class);
        assertThat(OrderMath.distance(Integer.MAX_VALUE, 0)).isEqualTo(Integer.MAX_VALUE);
    }
}
