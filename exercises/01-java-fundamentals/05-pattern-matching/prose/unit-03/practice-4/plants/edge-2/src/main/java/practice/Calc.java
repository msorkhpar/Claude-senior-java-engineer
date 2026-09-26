package practice;

public final class Calc {

    /** An arithmetic expression. */
    public sealed interface Expr permits Num, Add, Sub, Mul, Neg {
    }

    /** A number. */
    public record Num(int value) implements Expr {
    }

    /** left + right. */
    public record Add(Expr left, Expr right) implements Expr {
    }

    /** left - right. */
    public record Sub(Expr left, Expr right) implements Expr {
    }

    /** left * right. */
    public record Mul(Expr left, Expr right) implements Expr {
    }

    /** -operand. */
    public record Neg(Expr operand) implements Expr {
    }

    private Calc() {
    }

    /** Evaluates the expression. */
    public static int eval(Expr expr) {
        return switch (expr) {
            case Num(var value) -> value;
            case Add(var left, var right) -> eval(left) + eval(right);
            case Sub(var left, var right) -> eval(left) - eval(right);
            case Mul(var left, var right) -> eval(left) * eval(right);
            default -> 0;
        };
    }
}
