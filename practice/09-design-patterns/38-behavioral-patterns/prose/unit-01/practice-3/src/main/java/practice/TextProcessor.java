package practice;

import java.util.function.UnaryOperator;

public final class TextProcessor {

    public static final UnaryOperator<String> UPPER_CASE = s -> {
        throw new UnsupportedOperationException("TODO");
    };
    public static final UnaryOperator<String> LOWER_CASE = s -> {
        throw new UnsupportedOperationException("TODO");
    };
    public static final UnaryOperator<String> REVERSE = s -> {
        throw new UnsupportedOperationException("TODO");
    };
    public static final UnaryOperator<String> TRIM_AND_UPPER = s -> {
        throw new UnsupportedOperationException("TODO");
    };
    public static final UnaryOperator<String> REMOVE_WHITESPACE = s -> {
        throw new UnsupportedOperationException("TODO");
    };

    public TextProcessor(UnaryOperator<String> strategy) {
        throw new UnsupportedOperationException("TODO");
    }

    public void setStrategy(UnaryOperator<String> strategy) {
        throw new UnsupportedOperationException("TODO");
    }

    public String process(String text) {
        throw new UnsupportedOperationException("TODO");
    }
}
