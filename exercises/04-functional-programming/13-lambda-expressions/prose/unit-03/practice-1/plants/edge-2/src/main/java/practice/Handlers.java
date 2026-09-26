package practice;

import java.util.ArrayList;
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
        return sink::add;
    }

    public static Supplier<List<String>> freshList() {
        return ArrayList::new;
    }

    public static Function<String, Integer> length() {
        return String::length;
    }

    public static Predicate<String> startsWithA() {
        return s -> s.startsWith("A");
    }

    public static BinaryOperator<Integer> larger() {
        return Integer::max;
    }

    public static Consumer<String> both(Consumer<String> first, Consumer<String> second) {
        return second.andThen(first);
    }
}
