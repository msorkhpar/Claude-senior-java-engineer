package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

class ChaosRunTest {

    private static final String LXM = "L128X256MixRandom";

    @Test
    void failsTheRequestedShareOfCallsWithinRange() {
        ChaosRun run = new ChaosRun(LXM, 90_210L);

        assertThat(run.failingCalls(0, 40, 0)).isEmpty();
        assertThat(run.failingCalls(1, 40, 100)).hasSize(40);
        List<Integer> some = run.failingCalls(2, 500, 30);
        assertThat(some).isSorted().doesNotHaveDuplicates().allSatisfy(call -> assertThat(call).isBetween(0, 499));
        assertThat(some.size()).isBetween(50, 250);
    }

    @Test
    void aRunWithTheSameAlgorithmAndSeedReplaysEveryWorker() {
        ChaosRun recorded = new ChaosRun(LXM, 4_417L);
        List<List<Integer>> first = new ArrayList<>();
        for (int worker = 0; worker < 3; worker++) first.add(recorded.failingCalls(worker, 300, 20));

        ChaosRun replay = new ChaosRun(LXM, 4_417L);
        for (int worker = 2; worker >= 0; worker--) {
            assertThat(replay.failingCalls(worker, 300, 20)).isEqualTo(first.get(worker));
        }
    }

    @Test
    void workersOfOneRunFailDifferentCalls() {
        ChaosRun run = new ChaosRun(LXM, 66L);

        assertThat(run.failingCalls(0, 300, 20)).isNotEqualTo(run.failingCalls(1, 300, 20));
    }

    @Test
    void noTwoWorkersDrawFromTheSameGenerator() {
        ChaosRun run = new ChaosRun(LXM, 812L);

        RandomGenerator first = run.generatorFor(0);
        RandomGenerator second = run.generatorFor(1);

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void aRunNamedWithTheLegacyAlgorithmDrawsFromRandom() {
        ChaosRun run = new ChaosRun("Random", 5L);

        assertThat(run.generatorFor(0)).isInstanceOf(Random.class);
        assertThat(run.generatorFor(3)).isInstanceOf(Random.class);
    }
}
