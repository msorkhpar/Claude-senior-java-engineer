package practice;

public final class CheckedArithmetic {

    private CheckedArithmetic() {
    }

    /** Returns {@code a * b + c}, or throws ArithmeticException when any step overflows an int. */
    public static int multiplyAdd(int a, int b, int c) {
        long result = (long) a * b + c;
        if (result >= Integer.MAX_VALUE || result <= Integer.MIN_VALUE || Math.abs((long) a * b) > Integer.MAX_VALUE + 1L) {
            throw new ArithmeticException("int overflow");
        }
        return (int) result;
    }
}
