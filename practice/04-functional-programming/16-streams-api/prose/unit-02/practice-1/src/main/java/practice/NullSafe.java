package practice;

import java.util.List;

public final class NullSafe {

    private NullSafe() {
    }

    /** A person whose nickname may be null. */
    public record Person(String name, String nickname) {
    }

    /** Returns the non-null nicknames, in the order of the people. */
    public static List<String> nicknames(List<Person> people) {
        throw new UnsupportedOperationException("write nicknames");
    }

    /** Returns how many items the array holds; a null array holds none. */
    public static long countItems(String[] items) {
        throw new UnsupportedOperationException("write countItems");
    }
}
