package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CalcTest {

    private static Calc.Expr num(long value) {
        return new Calc.Num(value);
    }

    @Test
    void evaluatesAndPrintsAnExpression() {
        Calc.Expr e = new Calc.Add(num(1), new Calc.Mul(num(2), num(3)));
        assertThat(Calc.eval(e)).isEqualTo(7);
        assertThat(Calc.show(e)).isEqualTo("1 + 2 * 3");
        assertThat(Calc.eval(new Calc.Neg(num(4)))).isEqualTo(-4);
        assertThat(Calc.show(new Calc.Neg(num(4)))).isEqualTo("-4");
    }

    @Test
    void aSumInsideAProductKeepsItsParentheses() {
        Calc.Expr e = new Calc.Mul(new Calc.Add(num(1), num(2)), num(3));
        assertThat(Calc.eval(e)).isEqualTo(9);
        assertThat(Calc.show(e)).isEqualTo("(1 + 2) * 3");
    }

    @Test
    void aNegatedSumOrProductKeepsItsParentheses() {
        Calc.Expr sum = new Calc.Neg(new Calc.Add(num(1), num(2)));
        Calc.Expr product = new Calc.Neg(new Calc.Mul(num(2), num(3)));
        assertThat(Calc.eval(sum)).isEqualTo(-3);
        assertThat(Calc.show(sum)).isEqualTo("-(1 + 2)");
        assertThat(Calc.show(product)).isEqualTo("-(2 * 3)");
    }
}
