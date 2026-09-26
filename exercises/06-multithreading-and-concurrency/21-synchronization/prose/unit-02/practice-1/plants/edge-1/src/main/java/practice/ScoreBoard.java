package practice;

import java.util.function.IntUnaryOperator;

public final class ScoreBoard {

    private volatile int[] scores;

    public ScoreBoard(int size) {
        this.scores = new int[size];
    }

    /** Applies change to one slot's score, atomically, and returns the new score. */
    public int update(int slot, IntUnaryOperator change) {
        int next = change.applyAsInt(scores[slot]);
        scores[slot] = next;
        return next;
    }

    public int get(int slot) {
        return scores[slot];
    }

    public int[] snapshot() {
        return scores.clone();
    }
}
