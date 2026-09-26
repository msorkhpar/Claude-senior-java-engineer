package practice;

import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.IntUnaryOperator;

public final class ScoreBoard {

    private final AtomicIntegerArray scores;

    public ScoreBoard(int size) {
        this.scores = new AtomicIntegerArray(size);
    }

    /** Applies change to one slot's score, atomically, and returns the new score. */
    public int update(int slot, IntUnaryOperator change) {
        return scores.updateAndGet(slot, change);
    }

    public int get(int slot) {
        return scores.get(slot);
    }

    public int[] snapshot() {
        int[] copy = new int[scores.length()];
        for (int i = 0; i < copy.length; i++) {
            copy[i] = scores.get(i);
        }
        return copy;
    }
}
