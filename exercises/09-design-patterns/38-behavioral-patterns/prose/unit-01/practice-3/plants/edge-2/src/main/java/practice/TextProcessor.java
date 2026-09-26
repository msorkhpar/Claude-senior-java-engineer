package practice;

import java.util.Locale;
import java.util.function.UnaryOperator;

public final class TextProcessor {

    public static final UnaryOperator<String> UPPER_CASE = String::toUpperCase;
    public static final UnaryOperator<String> LOWER_CASE = String::toLowerCase;
    public static final UnaryOperator<String> REVERSE = s -> new StringBuilder(s).reverse().toString();
    public static final UnaryOperator<String> TRIM_AND_UPPER = s -> s.trim().toUpperCase();
    public static final UnaryOperator<String> REMOVE_WHITESPACE = s -> s.replaceAll("\\s+", "");

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
