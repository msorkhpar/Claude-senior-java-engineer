package practice;

import java.util.Map;

public final class Fibonacci {

    private Fibonacci() {
    }

    /** The n-th Fibonacci number, using and filling memo. */
    public static long fib(int n, Map<Integer, Long> memo) {
        if (n < 0) {
            throw new IllegalArgumentException("n must not be negative: " + n);
        }
        if (n < 2) {
            return n;
        }
        Long known = memo.get(n);
        if (known != null) {
            return known;
        }
        long value = fib(n - 1, memo) + fib(n - 2, memo);
        memo.put(n, value);
        return value;
    }
}
