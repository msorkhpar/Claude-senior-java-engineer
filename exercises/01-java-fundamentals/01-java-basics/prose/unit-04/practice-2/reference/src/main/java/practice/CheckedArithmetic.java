package practice;

public final class CheckedArithmetic {

    private CheckedArithmetic() {
    }

    /** Returns {@code a * b + c}, or throws ArithmeticException when any step overflows an int. */
    public static int multiplyAdd(int a, int b, int c) {
        return Math.addExact(Math.multiplyExact(a, b), c);
    }
}
