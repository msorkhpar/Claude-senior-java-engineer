package practice;

import java.util.List;

public final class BatchChecks {

    private BatchChecks() {
    }

    /** Whether every word has more than {@code minLength} characters. */
    public static boolean allLongerThan(List<String> words, int minLength) {
        return words.stream().allMatch(w -> w.length() > minLength);
    }

    /** Whether at least one word has more than {@code minLength} characters. */
    public static boolean anyLongerThan(List<String> words, int minLength) {
        return words.stream().anyMatch(w -> w.length() > minLength);
    }

    /** Whether no word starts with {@code prefix}. */
    public static boolean noneStartsWith(List<String> words, String prefix) {
        return !words.isEmpty() && words.stream().noneMatch(w -> w.startsWith(prefix));
    }
}
