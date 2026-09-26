package practice;

import java.util.Map;

public final class Scores {

    private Scores() {
    }

    /** Removes every entry scored below min, in place, and returns how many were removed. */
    public static int dropBelow(Map<String, Integer> scores, int min) {
        int before = scores.size();
        scores.entrySet().removeIf(entry -> entry.getValue() - min < 0);
        return before - scores.size();
    }
}
