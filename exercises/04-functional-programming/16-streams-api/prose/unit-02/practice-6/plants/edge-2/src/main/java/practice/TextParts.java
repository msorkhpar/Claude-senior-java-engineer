package practice;

import java.util.Arrays;
import java.util.List;

public final class TextParts {

    private TextParts() {
    }

    /** Counts the vowels of {@code text}, in either case. */
    public static long countVowels(String text) {
        return text.chars()
                .filter(c -> "aeiouAEIOU".indexOf(c) >= 0)
                .count();
    }

    /** Splits {@code text} at the literal {@code delimiter}; returns the trimmed, non-empty parts. */
    public static List<String> parts(String text, String delimiter) {
        return Arrays.stream(text.split(delimiter))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }
}
