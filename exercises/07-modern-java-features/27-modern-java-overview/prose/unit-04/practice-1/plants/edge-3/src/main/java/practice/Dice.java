package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

public final class Dice {

    private Dice() {
    }

    /** {@code count} rolls from 1 to {@code sides}, drawn from {@code rng}. */
    public static List<Integer> roll(RandomGenerator rng, int count, int sides) {
        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(rng.nextInt(1, sides + 1));
        }
        return rolls;
    }
}
