package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.assertThat;

class SeedsTest {

    private static final String MIX = "L64X128MixRandom";

    @Test
    void samplesFromAnAvailableAlgorithm() {
        assertThat(Seeds.available(MIX)).isTrue();
        assertThat(Seeds.available("Xoroshiro128PlusPlus")).isTrue();
        assertThat(Seeds.sample(MIX, 42, 5, 100)).hasSize(5).allSatisfy(v -> assertThat(v).isBetween(0, 99));
        assertThat(Seeds.sample(MIX, 0, 3, 10)).hasSize(3);
    }

    @Test
    void unknownAlgorithmsAreUnavailable() {
        assertThat(Seeds.available("NoSuchAlgorithm")).isFalse();
        assertThat(Seeds.available("")).isFalse();
        assertThat(Seeds.available("l64x128mixrandom")).isFalse();
    }

    @Test
    void sameSeedSameSample() {
        List<Integer> first = Seeds.sample(MIX, 42, 20, 1000);
        assertThat(Seeds.sample(MIX, 42, 20, 1000)).isEqualTo(first);
        List<Integer> expected = RandomGeneratorFactory.of(MIX).create(42L).ints(20, 0, 1000).boxed().toList();
        assertThat(first).isEqualTo(expected);
        List<Integer> other = RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(7L).ints(20, 0, 1000).boxed().toList();
        assertThat(Seeds.sample("Xoroshiro128PlusPlus", 7, 20, 1000)).isEqualTo(other);
        long big = 1L << 32;
        List<Integer> wide = RandomGeneratorFactory.of(MIX).create(big).ints(20, 0, 1000).boxed().toList();
        assertThat(Seeds.sample(MIX, big, 20, 1000)).isEqualTo(wide);
    }

    @Test
    void otherSeedOtherSample() {
        assertThat(Seeds.sample(MIX, 1, 20, 1000)).isNotEqualTo(Seeds.sample(MIX, 2, 20, 1000));
    }
}
