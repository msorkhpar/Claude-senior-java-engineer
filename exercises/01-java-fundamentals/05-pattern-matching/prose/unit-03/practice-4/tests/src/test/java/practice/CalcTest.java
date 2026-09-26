package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CalcTest {

    @Test
    void evaluatesSumsAndProducts() {
        assertThat(Calc.eval(new Calc.Num(7))).isEqualTo(7);
        assertThat(Calc.eval(new Calc.Add(new Calc.Num(2), new Calc.Mul(new Calc.Num(3), new Calc.Num(4))))).isEqualTo(14);
        assertThat(Calc.eval(new Calc.Mul(new Calc.Add(new Calc.Num(1), new Calc.Num(1)), new Calc.Num(5)))).isEqualTo(10);
    }

    @Test
    void subtractionTakesRightFromLeft() {
        assertThat(Calc.eval(new Calc.Sub(new Calc.Num(10), new Calc.Num(4)))).isEqualTo(6);
        assertThat(Calc.eval(new Calc.Sub(new Calc.Num(1), new Calc.Num(3)))).isEqualTo(-2);
    }

    @Test
    void negationIsEvaluated() {
        assertThat(Calc.eval(new Calc.Neg(new Calc.Num(5)))).isEqualTo(-5);
        assertThat(Calc.eval(new Calc.Neg(new Calc.Neg(new Calc.Num(2))))).isEqualTo(2);
    }
}
