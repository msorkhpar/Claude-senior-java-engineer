package practice;

public final class Factorial {

    private Factorial() {
    }

    /** Returns n! for 0 <= n <= 20; refuses a negative n and an overflowing result. */
    public static long of(int n) {
        long result = 1;
        int i = 1;
        do {
            result = Math.multiplyExact(result, i);
            i++;
        } while (i <= n);
        return result;
    }
}
