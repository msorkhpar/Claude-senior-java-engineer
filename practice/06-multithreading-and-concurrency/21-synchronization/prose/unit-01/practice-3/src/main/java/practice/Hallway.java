package practice;

public final class Hallway {

    /** Whether each walker got through, and how many rounds each one took part in. */
    public record Outcome(boolean walker1Done, boolean walker2Done, int walker1Rounds, int walker2Rounds) {
    }

    /** Two polite walkers in lock step: each steps forward every round. */
    public static Outcome runPolite(int maxRounds) throws InterruptedException {
        throw new UnsupportedOperationException("write runPolite");
    }

    /** The same walkers, with random backoff after a conflict: walker 1 uses new Random(seed), walker 2 new Random(seed + 1). */
    public static Outcome runWithBackoff(int maxRounds, long seed) throws InterruptedException {
        throw new UnsupportedOperationException("write runWithBackoff");
    }
}
