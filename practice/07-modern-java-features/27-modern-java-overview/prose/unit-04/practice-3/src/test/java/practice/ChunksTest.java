package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ChunksTest {

    private static RandomGenerator.JumpableGenerator seeded(long seed) {
        return (RandomGenerator.JumpableGenerator) RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(seed);
    }

    private static List<Integer> draw(RandomGenerator generator, int n) {
        return generator.ints(n, 0, 100).boxed().toList();
    }

    @Test
    void makesReproducibleChunks() {
        List<List<Integer>> chunks = Chunks.split(42, 3, 5);
        assertThat(chunks).hasSize(3).allSatisfy(chunk ->
                assertThat(chunk).hasSize(5).allSatisfy(v -> assertThat(v).isBetween(0, 99)));
        assertThat(Chunks.split(42, 3, 5)).isEqualTo(chunks);
        assertThat(Chunks.split(42, 0, 5)).isEmpty();
    }

    @Test
    void firstChunkIsTheSeededSequence() {
        assertThat(Chunks.split(42, 3, 5).get(0)).isEqualTo(draw(seeded(42), 5));
        assertThat(Chunks.split(7, 1, 8).get(0)).isEqualTo(draw(seeded(7), 8));
        long big = 1L << 32;
        assertThat(Chunks.split(big, 1, 8).get(0)).isEqualTo(draw(seeded(big), 8));
    }

    @Test
    void laterChunksFollowAJump() {
        RandomGenerator.JumpableGenerator generator = seeded(42);
        draw(generator, 5);
        generator.jump();
        List<Integer> second = draw(generator, 5);
        generator.jump();
        List<Integer> third = draw(generator, 5);
        List<List<Integer>> chunks = Chunks.split(42, 3, 5);
        assertThat(chunks.get(1)).isEqualTo(second);
        assertThat(chunks.get(2)).isEqualTo(third);
    }
}
