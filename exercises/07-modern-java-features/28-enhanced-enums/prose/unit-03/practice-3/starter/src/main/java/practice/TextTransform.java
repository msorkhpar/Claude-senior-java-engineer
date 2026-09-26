package practice;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum TextTransform {
    UPPER_CASE("Upper Case", todo()),
    LOWER_CASE("Lower Case", todo()),
    TRIM("Trim", todo()),
    REVERSE("Reverse", todo()),
    CAPITALIZE("Capitalize", todo()),
    SNAKE_CASE("Snake Case", todo());

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
        throw new UnsupportedOperationException("write apply");
    }

    /** Applies {@code transforms} one after another, left to right. */
    public static String applyAll(String input, TextTransform... transforms) {
        throw new UnsupportedOperationException("write applyAll");
    }

    private static UnaryOperator<String> todo() {
        return s -> {
            throw new UnsupportedOperationException("write this operator");
        };
    }
}
