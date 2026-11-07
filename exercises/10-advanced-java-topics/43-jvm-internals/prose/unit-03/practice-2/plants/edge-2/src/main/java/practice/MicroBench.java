package practice;

import java.util.function.LongSupplier;
import java.util.function.LongUnaryOperator;

/**
 * The team's micro-benchmark helper. It times a piece of work over a set of inputs, after some
 * rounds of warm-up, and reports the time per call. JMH isn't on the build's class path.
 */
public class MicroBench {

    /** Nanoseconds per timed call, and a checksum of every value the work returned. */
    public record Report(double nanosPerCall, long checksum) {}

    private final LongSupplier clock;

    /** @param clock nanoseconds from some fixed origin; production passes System::nanoTime */
    public MicroBench(LongSupplier clock) {
        this.clock = clock;
    }

    public static MicroBench withSystemClock() {
        return new MicroBench(System::nanoTime);
    }

    /**
     * Runs {@code work} over every input {@code warmupRounds} times, then once more over every
     * input, timed, and reports the time per call of that last round.
     */
    public Report measure(LongUnaryOperator work, long[] inputs, int warmupRounds) {
        long checksum = 0;
        for (int round = 0; round < warmupRounds; round++) {
            for (long input : inputs) checksum += work.applyAsLong(input);
        }
        long start = clock.getAsLong();
        long first = inputs[0];                                      // keep array reads out of the timing
        for (int i = 0; i < inputs.length; i++) checksum += work.applyAsLong(first);
        long elapsed = clock.getAsLong() - start;
        return new Report(elapsed / (double) inputs.length, checksum);
    }
}
