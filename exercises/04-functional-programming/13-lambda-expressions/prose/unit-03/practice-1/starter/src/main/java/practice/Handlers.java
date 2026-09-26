package practice;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Handlers {

    private Handlers() {
    }

    public static Consumer<String> appendTo(List<String> sink) {
        throw new UnsupportedOperationException("write appendTo");
    }

    public static Supplier<List<String>> freshList() {
        throw new UnsupportedOperationException("write freshList");
    }

    public static Function<String, Integer> length() {
        throw new UnsupportedOperationException("write length");
    }

    public static Predicate<String> startsWithA() {
        throw new UnsupportedOperationException("write startsWithA");
    }

    public static BinaryOperator<Integer> larger() {
        throw new UnsupportedOperationException("write larger");
    }

    public static Consumer<String> both(Consumer<String> first, Consumer<String> second) {
        throw new UnsupportedOperationException("write both");
    }
}
