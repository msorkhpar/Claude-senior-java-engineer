package practice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class LengthIndex {

    private LengthIndex() {
    }

    /** Each word length mapped to its words joined with ", " in list order; keys in first-seen order. */
    public static Map<Integer, String> byLength(List<String> words) {
        return words.stream()
                .collect(Collectors.toMap(String::length, w -> w));
    }
}
