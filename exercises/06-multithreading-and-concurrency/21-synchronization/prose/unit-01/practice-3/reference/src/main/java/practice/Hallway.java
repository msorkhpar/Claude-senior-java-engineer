package practice;

import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Hallway {

    /** Whether each walker got through, and how many rounds each one took part in. */
    public record Outcome(boolean walker1Done, boolean walker2Done, int walker1Rounds, int walker2Rounds) {
    }

    private final AtomicBoolean[] stepsForward = {new AtomicBoolean(), new AtomicBoolean()};
    private final Phaser lockStep = new Phaser(2);
    private final Random[] randoms;
    private final int maxRounds;

    private Hallway(Random[] randoms, int maxRounds) {
        this.randoms = randoms;
        this.maxRounds = maxRounds;
    }

    /** Two polite walkers in lock step: each steps forward every round. */
    public static Outcome runPolite(int maxRounds) throws InterruptedException {
        return new Hallway(null, maxRounds).run();
    }

    /** The same walkers, with random backoff after a conflict: walker 1 uses new Random(seed), walker 2 new Random(seed + 1). */
    public static Outcome runWithBackoff(int maxRounds, long seed) throws InterruptedException {
        return new Hallway(new Random[] {new Random(seed), new Random(seed + 1)}, maxRounds).run();
    }

    private Outcome run() throws InterruptedException {
        boolean[] done = new boolean[2];
        int[] rounds = new int[2];
        Thread w1 = new Thread(() -> walk(0, done, rounds), "walker-1");
        Thread w2 = new Thread(() -> walk(1, done, rounds), "walker-2");
        w1.setDaemon(true);
        w2.setDaemon(true);
        w1.start();
        w2.start();
        w1.join();
        w2.join();
        return new Outcome(done[0], done[1], rounds[0], rounds[1]);
    }

    private void walk(int me, boolean[] done, int[] rounds) {
        int other = 1 - me;
        boolean afterConflict = false;
        for (int round = 1; round <= maxRounds; round++) {
            rounds[me] = round;
            boolean stepForward = !afterConflict || randoms == null || randoms[me].nextBoolean();
            stepsForward[me].set(stepForward);
            lockStep.arriveAndAwaitAdvance();
            boolean goThrough = stepForward && !stepsForward[other].get();
            if (stepForward && !goThrough) {
                afterConflict = true;
            }
            lockStep.arriveAndAwaitAdvance();
            if (goThrough) {
                done[me] = true;
                stepsForward[me].set(false);
                lockStep.arriveAndDeregister();
                return;
            }
        }
        lockStep.arriveAndDeregister();
    }
}
