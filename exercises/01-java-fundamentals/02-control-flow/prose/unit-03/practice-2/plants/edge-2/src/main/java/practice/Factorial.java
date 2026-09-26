package practice;

public final class Factorial {

    private Factorial() {
    }

    /** Returns n! for 0 <= n <= 20; refuses a negative n and an overflowing result. */
    public static long of(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("no factorial for a negative number");
        }
        long result = 1;
        int i = 1;
        do {
            result *= i;
            i++;
        } while (i <= n);
        return result;
    }
}
