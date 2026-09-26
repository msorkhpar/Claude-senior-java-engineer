package practice;

import java.util.List;

public final class Mentions {

    private Mentions() {
    }

    /** The sources that contain term, in order. */
    public static List<String> mentioning(String term, List<String> sources) {
        throw new UnsupportedOperationException("write mentioning");
    }

    /** The candidates that text starts with, in order. */
    public static List<String> prefixesOf(String text, List<String> candidates) {
        throw new UnsupportedOperationException("write prefixesOf");
    }

    /** The requested values that are in allowList, in order. */
    public static List<String> allowed(List<String> requested, List<String> allowList) {
        throw new UnsupportedOperationException("write allowed");
    }
}
