package practice;

import java.util.function.IntUnaryOperator;

public final class ScoreBoard {

    private final int[] scores;

    public ScoreBoard(int size) {
        this.scores = new int[size];
    }

    /** Applies change to one slot's score, atomically, and returns the new score. */
    public synchronized int update(int slot, IntUnaryOperator change) {
        scores[slot] = change.applyAsInt(scores[slot]);
        return scores[slot];
    }

    public synchronized int get(int slot) {
        return scores[slot];
    }

    public synchronized int[] snapshot() {
        return scores.clone();
    }
}
