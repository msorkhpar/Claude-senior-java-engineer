package practice;

import java.util.List;

/** Thins out sensor readings for the charts: keeps every step-th reading. */
public class Downsampler {

    private final int step;

    public Downsampler(int step) {
        if (step < 1) throw new IllegalArgumentException("step must be at least 1: " + step);
        this.step = step;
    }

    /** The readings at positions 0, step, 2·step, … of {@code readings}, in order. */
    public <T> List<T> sample(List<T> readings) {
        return List.copyOf(pick(readings, step));
    }

    /** Picks the readings at positions 0, step, 2·step, … into a new list. */
    static <T> List<T> pick(List<T> readings, int step) {
        // TODO: The live dashboard passes lists of millions of readings and wants every
        //       thousandth; the archive passes the readings it has replayed from disk. Pick the
        //       readings at positions 0, step, 2*step, ... in order, in time that stays
        //       reasonable for both.
        throw new IllegalStateException("TODO: pick");
    }
}
