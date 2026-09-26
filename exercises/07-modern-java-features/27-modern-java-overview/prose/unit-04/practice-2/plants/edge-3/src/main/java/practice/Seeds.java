package practice;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

public final class Seeds {

    private Seeds() {
    }

    /** Whether the JVM has an algorithm with exactly this name. */
    public static boolean available(String algorithm) {
        return RandomGeneratorFactory.all().anyMatch(factory -> factory.name().equals(algorithm));
    }

    /** {@code count} values in [0, bound) from the algorithm's generator seeded with {@code seed}. */
    public static List<Integer> sample(String algorithm, long seed, int count, int bound) {
        return RandomGeneratorFactory.of(algorithm).create(42L).ints(count, 0, bound).boxed().toList();
    }
}
