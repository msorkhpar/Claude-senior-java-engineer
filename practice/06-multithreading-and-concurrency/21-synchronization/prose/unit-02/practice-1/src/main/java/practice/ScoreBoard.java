package practice;

import java.util.function.IntUnaryOperator;

public final class ScoreBoard {

    public ScoreBoard(int size) {
    }

    /** Applies change to one slot's score, atomically, and returns the new score. */
    public int update(int slot, IntUnaryOperator change) {
        throw new UnsupportedOperationException("write update");
    }

    public int get(int slot) {
        throw new UnsupportedOperationException("write get");
    }

    public int[] snapshot() {
        throw new UnsupportedOperationException("write snapshot");
    }
}
