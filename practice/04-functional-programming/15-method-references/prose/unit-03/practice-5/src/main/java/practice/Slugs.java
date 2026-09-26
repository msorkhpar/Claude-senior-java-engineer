package practice;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Slugs {

    private Slugs() {
    }

    /** Trimmed, lower-cased, each run of whitespace replaced by one dash. */
    public static Function<String, String> slug() {
        throw new UnsupportedOperationException("write slug");
    }

    /** The length of the slug of the argument: String::length composed with slug(). */
    public static Function<String, Integer> slugLength() {
        throw new UnsupportedOperationException("write slugLength");
    }

    /** The length of the two strings joined: String::concat, then String::length. */
    public static BiFunction<String, String, Integer> joinedLength() {
        throw new UnsupportedOperationException("write joinedLength");
    }
}
