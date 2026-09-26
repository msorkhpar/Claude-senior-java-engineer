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
        throw new UnsupportedOperationException("write upper");
    }

    /** s -> Integer.parseInt(s), as a method reference. */
    public static Function<String, Integer> parse() {
        throw new UnsupportedOperationException("write parse");
    }

    /** () -> new ArrayList<>(), as a constructor reference. */
    public static Supplier<ArrayList<String>> newList() {
        throw new UnsupportedOperationException("write newList");
    }

    /** (s1, s2) -> s1.compareTo(s2), as a method reference. */
    public static Comparator<String> compare() {
        throw new UnsupportedOperationException("write compare");
    }

    /** s -> new StringBuilder(s), as a constructor reference. */
    public static Function<String, StringBuilder> builder() {
        throw new UnsupportedOperationException("write builder");
    }
}
