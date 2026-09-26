package practice;

import java.util.List;

public final class WordOrder {

    private WordOrder() {
    }

    /** Sorted by String::compareTo. */
    public static List<String> natural(List<String> words) {
        throw new UnsupportedOperationException("write natural");
    }

    /** Sorted by String::compareToIgnoreCase. */
    public static List<String> ignoringCase(List<String> words) {
        throw new UnsupportedOperationException("write ignoringCase");
    }

    /** Sorted by length, then alphabetically. */
    public static List<String> shortestFirst(List<String> words) {
        throw new UnsupportedOperationException("write shortestFirst");
    }

    /** The lists that are not empty, in order. */
    public static List<List<String>> nonEmpty(List<List<String>> lists) {
        throw new UnsupportedOperationException("write nonEmpty");
    }
}
