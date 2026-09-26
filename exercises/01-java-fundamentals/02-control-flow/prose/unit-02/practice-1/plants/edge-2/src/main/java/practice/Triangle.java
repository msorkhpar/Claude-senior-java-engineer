package practice;

public final class Triangle {

    private Triangle() {
    }

    /** Returns 1 + 2 + ... + n, or 0 when n is below 1. */
    public static long sum(int n) {
        int total = 0;
        for (int i = 1; i <= n; i++) {
            total += i;
        }
        return total;
    }
}
