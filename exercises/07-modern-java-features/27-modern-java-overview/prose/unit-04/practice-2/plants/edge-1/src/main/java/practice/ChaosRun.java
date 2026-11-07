package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Decides which calls fail in one chaos run against the payment gateway. The run is named by
 * its algorithm and seed, which the report prints, so a failure seen in a run can be replayed.
 * Each worker of the run calls failingCalls on its own thread, all of them at the same time.
 */
public class ChaosRun {

    private final String algorithm;
    private final long seed;

    public ChaosRun(String algorithm, long seed) {
        this.algorithm = algorithm;
        this.seed = seed;
    }

    /**
     * The indexes, from 0 to calls - 1, of the calls this worker makes fail: one nextInt(100) draw
     * per call, and the call fails when the draw is below percent.
     */
    public List<Integer> failingCalls(int worker, int calls, int percent) {
        RandomGenerator random = generatorFor(worker);
        List<Integer> failing = new ArrayList<>();
        for (int call = 0; call < calls; call++) {
            if (random.nextInt(100) < percent) failing.add(call);
        }
        return failing;
    }

    /** The generator this worker draws from in this run. */
    RandomGenerator generatorFor(int worker) {
        return RandomGenerator.of(algorithm);
    }
}
