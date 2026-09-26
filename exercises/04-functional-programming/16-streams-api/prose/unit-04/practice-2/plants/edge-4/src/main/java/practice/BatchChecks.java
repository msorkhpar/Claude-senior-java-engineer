package practice;

import java.util.List;
import java.util.Locale;

public final class BatchChecks {

    private BatchChecks() {
    }

    public static boolean allLongerThan(List<String> words, int minLength) {
        return words.stream().allMatch(w -> w.trim().length() > minLength);
    }

    public static boolean anyLongerThan(List<String> words, int minLength) {
        return words.stream().anyMatch(w -> w.trim().length() > minLength);
    }

    public static boolean noneStartsWith(List<String> words, String prefix) {
        return words.stream().noneMatch(w -> w.startsWith(prefix));
    }
}
