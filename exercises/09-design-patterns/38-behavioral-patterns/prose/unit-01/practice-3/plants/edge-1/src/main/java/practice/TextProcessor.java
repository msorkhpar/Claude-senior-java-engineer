package practice;

import java.util.Locale;
import java.util.function.UnaryOperator;

public final class TextProcessor {

    public static final UnaryOperator<String> UPPER_CASE = s -> s.toUpperCase(Locale.ROOT);
    public static final UnaryOperator<String> LOWER_CASE = s -> s.toLowerCase(Locale.ROOT);
    public static final UnaryOperator<String> REVERSE = s -> new StringBuilder(s).reverse().toString();
    public static final UnaryOperator<String> TRIM_AND_UPPER = s -> s.trim().toUpperCase(Locale.ROOT);
    public static final UnaryOperator<String> REMOVE_WHITESPACE = s -> s.replace(" ", "");

    private UnaryOperator<String> strategy;

    public TextProcessor(UnaryOperator<String> strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(UnaryOperator<String> strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy cannot be null");
        }
        this.strategy = strategy;
    }

    public String process(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }
        return strategy.apply(text);
    }
}
