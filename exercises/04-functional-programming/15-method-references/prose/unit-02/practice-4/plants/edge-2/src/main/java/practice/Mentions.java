package practice;

import java.util.List;

public final class Mentions {

    private Mentions() {
    }

    /** The sources that contain term, in order. */
    public static List<String> mentioning(String term, List<String> sources) {
        return sources.stream().filter(source -> source.contains(term)).toList();
    }

    /** The candidates that text starts with, in order. */
    public static List<String> prefixesOf(String text, List<String> candidates) {
        return candidates.stream().filter(candidate -> candidate.startsWith(text)).toList();
    }

    /** The requested values that are in allowList, in order. */
    public static List<String> allowed(List<String> requested, List<String> allowList) {
        return requested.stream().filter(allowList::contains).toList();
    }
}
