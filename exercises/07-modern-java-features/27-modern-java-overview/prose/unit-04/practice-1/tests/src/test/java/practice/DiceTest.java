package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiceTest {

    private static RandomGenerator mix(long seed) {
        return RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
    }

    @Test
    void rollsWithEveryKindOfGenerator() {
        for (RandomGenerator rng : List.of(new Random(7), new SplittableRandom(7), mix(7))) {
            List<Integer> rolls = Dice.roll(rng, 200, 6);
            assertThat(rolls).hasSize(200).allSatisfy(r -> assertThat(r).isBetween(1, 6));
        }
        assertThat(Dice.roll(new SplittableRandom(1), 0, 6)).isEmpty();
    }

    @Test
    void usesTheGivenGenerator() {
        assertThat(Dice.roll(mix(42), 50, 20)).isEqualTo(Dice.roll(mix(42), 50, 20));
        List<Integer> expected = new Random(99).ints(50, 1, 21).boxed().toList();
        assertThat(Dice.roll(new Random(99), 50, 20)).isEqualTo(expected);
    }

    @Test
    void topFaceComesUp() {
        List<Integer> rolls = Dice.roll(mix(42), 1000, 6);
        assertThat(rolls).contains(1, 6).allSatisfy(r -> assertThat(r).isBetween(1, 6));
    }

    @Test
    void negativeCountIsRefused() {
        assertThatThrownBy(() -> Dice.roll(new Random(7), -1, 6)).isInstanceOf(IllegalArgumentException.class);
    }
}
