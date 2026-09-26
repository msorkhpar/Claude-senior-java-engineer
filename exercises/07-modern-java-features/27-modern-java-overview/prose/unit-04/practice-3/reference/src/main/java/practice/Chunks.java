package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class Chunks {

    private Chunks() {
    }

    /** {@code chunks} lists of {@code perChunk} values, the generator jumping after each chunk. */
    public static List<List<Integer>> split(long seed, int chunks, int perChunk) {
        RandomGenerator.JumpableGenerator generator = (RandomGenerator.JumpableGenerator)
                RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(seed);
        List<List<Integer>> out = new ArrayList<>();
        for (int i = 0; i < chunks; i++) {
            out.add(generator.ints(perChunk, 0, 100).boxed().toList());
            generator.jump();
        }
        return out;
    }
}
