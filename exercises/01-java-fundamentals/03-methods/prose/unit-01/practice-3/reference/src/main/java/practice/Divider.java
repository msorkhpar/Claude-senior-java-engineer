package practice;

public final class Divider {

    private Divider() {
    }

    /** Returns numerator / denominator, refusing a zero denominator. */
    public static double divide(double numerator, double denominator) throws ArithmeticException {
        if (denominator == 0) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return numerator / denominator;
    }
}
