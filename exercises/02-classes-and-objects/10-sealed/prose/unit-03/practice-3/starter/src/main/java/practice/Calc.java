package practice;

public class Calc {

    public sealed interface Expr permits Num, Add, Mul, Neg {
    }

    public record Num(long value) implements Expr {
    }

    public record Add(Expr left, Expr right) implements Expr {
    }

    public record Mul(Expr left, Expr right) implements Expr {
    }

    public record Neg(Expr operand) implements Expr {
    }

    public static long eval(Expr e) {
        throw new UnsupportedOperationException("write eval");
    }

    public static String show(Expr e) {
        throw new UnsupportedOperationException("write show");
    }
}
