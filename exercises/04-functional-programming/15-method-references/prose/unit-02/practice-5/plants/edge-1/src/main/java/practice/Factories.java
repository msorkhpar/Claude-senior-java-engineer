package practice;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Factories {

    private Factories() {
    }

    /** count objects made by factory. */
    public static <C> List<C> buckets(int count, Supplier<C> factory) {
        return Collections.nCopies(count, factory.get());
    }

    /** One object per name, made by factory, in order. */
    public static <T> List<T> build(List<String> names, Function<String, T> factory) {
        return names.stream().map(factory).toList();
    }

    /** The words upper-cased, as a String[]. */
    public static String[] shout(List<String> words) {
        return words.stream().map(String::toUpperCase).toArray(String[]::new);
    }
}
