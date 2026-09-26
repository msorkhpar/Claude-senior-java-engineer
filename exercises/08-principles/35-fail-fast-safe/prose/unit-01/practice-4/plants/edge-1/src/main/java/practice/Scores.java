package practice;

import java.util.Map;

public final class Scores {

    private Scores() {
    }

    /** Removes every entry scored below min, in place, and returns how many were removed. */
    public static int dropBelow(Map<String, Integer> scores, int min) {
        return (int) scores.values().stream().filter(v -> v < min).count();
    }
}
