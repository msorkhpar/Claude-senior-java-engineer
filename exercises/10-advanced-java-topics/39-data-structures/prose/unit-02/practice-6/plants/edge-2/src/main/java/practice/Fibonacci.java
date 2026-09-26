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
        memo.clear();
        return compute(n, memo);
    }

    private static long compute(int n, Map<Integer, Long> memo) {
        if (n < 2) {
            return n;
        }
        Long known = memo.get(n);
        if (known != null) {
            return known;
        }
        long value = Math.addExact(compute(n - 1, memo), compute(n - 2, memo));
        memo.put(n, value);
        return value;
    }
}
