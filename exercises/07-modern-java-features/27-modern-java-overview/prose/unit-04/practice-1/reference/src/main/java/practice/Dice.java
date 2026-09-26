package practice;

import java.util.List;
import java.util.random.RandomGenerator;

public final class Dice {

    private Dice() {
    }

    /** {@code count} rolls from 1 to {@code sides}, drawn from {@code rng}. */
    public static List<Integer> roll(RandomGenerator rng, int count, int sides) {
        return rng.ints(count, 1, sides + 1).boxed().toList();
    }
}
