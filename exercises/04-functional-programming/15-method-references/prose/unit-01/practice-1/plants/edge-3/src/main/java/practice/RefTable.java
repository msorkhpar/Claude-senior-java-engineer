package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public final class RefTable {

    private RefTable() {
    }

    /** s -> s.toUpperCase(), as a method reference. */
    public static Function<String, String> upper() {
        return String::toUpperCase;
    }

    /** s -> Integer.parseInt(s), as a method reference. */
    public static Function<String, Integer> parse() {
        return Integer::parseInt;
    }

    /** () -> new ArrayList<>(), as a constructor reference. */
    public static Supplier<ArrayList<String>> newList() {
        return ArrayList::new;
    }

    /** (s1, s2) -> s1.compareTo(s2), as a method reference. */
    public static Comparator<String> compare() {
        return String::compareTo;
    }

    /** s -> new StringBuilder(s), as a constructor reference. */
    public static Function<String, StringBuilder> builder() {
        return new StringBuilder()::append;
    }
}
