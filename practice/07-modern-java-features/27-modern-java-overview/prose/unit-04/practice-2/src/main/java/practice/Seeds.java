package practice;

import java.util.List;

public final class Seeds {

    private Seeds() {
    }

    /** Whether the JVM has an algorithm with exactly this name. */
    public static boolean available(String algorithm) {
        throw new UnsupportedOperationException("write available");
    }

    /** {@code count} values in [0, bound) from the algorithm's generator seeded with {@code seed}. */
    public static List<Integer> sample(String algorithm, long seed, int count, int bound) {
        throw new UnsupportedOperationException("write sample");
    }
}
