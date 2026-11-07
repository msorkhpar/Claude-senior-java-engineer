package practice;

import java.util.ArrayList;
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
        List<T> picked = new ArrayList<>();
        for (int i = 0; i < readings.size(); i += step) picked.add(readings.get(i));   // a List is a List
        return picked;
    }
}
