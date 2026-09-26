package practice;

import java.util.List;
import java.util.regex.Pattern;

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
        return Pattern.compile(Pattern.quote(delimiter))
                .splitAsStream(text)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .toList();
    }
}
