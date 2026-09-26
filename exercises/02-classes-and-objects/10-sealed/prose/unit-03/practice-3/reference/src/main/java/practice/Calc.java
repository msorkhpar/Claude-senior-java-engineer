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
        return switch (e) {
            case Num(long value) -> value;
            case Add(Expr left, Expr right) -> eval(left) + eval(right);
            case Mul(Expr left, Expr right) -> eval(left) * eval(right);
            case Neg(Expr operand) -> -eval(operand);
        };
    }

    public static String show(Expr e) {
        return switch (e) {
            case Num(long value) -> Long.toString(value);
            case Add(Expr left, Expr right) -> show(left) + " + " + show(right);
            case Mul(Expr left, Expr right) -> factor(left) + " * " + factor(right);
            case Neg(Expr operand) -> "-" + (operand instanceof Add || operand instanceof Mul ? "(" + show(operand) + ")" : show(operand));
        };
    }

    private static String factor(Expr e) {
        return e instanceof Add ? "(" + show(e) + ")" : show(e);
    }
}
