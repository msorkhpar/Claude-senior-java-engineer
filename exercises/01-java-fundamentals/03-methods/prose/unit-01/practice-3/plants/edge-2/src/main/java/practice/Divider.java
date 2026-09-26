package practice;

public final class Divider {

    private Divider() {
    }

    /** Returns numerator / denominator, refusing a zero denominator. */
    public static double divide(double numerator, double denominator) throws ArithmeticException {
        double result = numerator / denominator;
        if (Double.isInfinite(result)) {
            throw new ArithmeticException("Cannot divide by zero");
        }
        return result;
    }
}
