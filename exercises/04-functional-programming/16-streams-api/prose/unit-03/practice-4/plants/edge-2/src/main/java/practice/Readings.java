package practice;

import java.util.List;

public final class Readings {

    private Readings() {
    }

    /** Returns the readings before the first one at or above {@code threshold}. */
    public static List<Integer> warmup(List<Integer> readings, int threshold) {
        return readings.stream().takeWhile(reading -> reading < threshold).toList();
    }

    /** Returns the first reading at or above {@code threshold} and every reading after it. */
    public static List<Integer> afterWarmup(List<Integer> readings, int threshold) {
        return readings.stream().filter(reading -> reading >= threshold).toList();
    }
}
