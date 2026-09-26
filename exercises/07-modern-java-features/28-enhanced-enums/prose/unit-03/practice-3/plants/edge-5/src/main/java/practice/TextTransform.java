package practice;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum TextTransform {
    UPPER_CASE("Upper Case", String::toUpperCase),
    LOWER_CASE("Lower Case", String::toLowerCase),
    TRIM("Trim", String::trim),
    REVERSE("Reverse", s -> new StringBuilder(s).reverse().toString()),
    CAPITALIZE("Capitalize", s -> s.isEmpty() ? s
            : s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase()),
    SNAKE_CASE("Snake Case", s -> s.trim().replaceAll("\\s+", "_").toLowerCase());

    private final String displayName;
    private final UnaryOperator<String> transformer;

    TextTransform(String displayName, UnaryOperator<String> transformer) {
        this.displayName = displayName;
        this.transformer = transformer;
    }

    public String displayName() {
        return displayName;
    }

    /** Applies this constant's operator; a null input is refused. */
    public String apply(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input must not be null");
        }
        return transformer.apply(input);
    }

    /** Applies {@code transforms} one after another, left to right. */
    public static String applyAll(String input, TextTransform... transforms) {
        String result = input;
        for (TextTransform t : transforms) {
            result = t.apply(result);
        }
        return result;
    }
}
