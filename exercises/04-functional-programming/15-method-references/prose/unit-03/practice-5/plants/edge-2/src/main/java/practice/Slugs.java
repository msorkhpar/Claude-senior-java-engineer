package practice;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class Slugs {

    private Slugs() {
    }

    /** Trimmed, lower-cased, each run of whitespace replaced by one dash. */
    public static Function<String, String> slug() {
        Function<String, String> trim = String::trim;
        return trim.andThen(String::toLowerCase).andThen(Slugs::dashes);
    }

    /** The length of the slug of the argument: String::length composed with slug(). */
    public static Function<String, Integer> slugLength() {
        Function<String, Integer> length = String::length;
        return length.compose(slug());
    }

    /** The length of the two strings joined: String::concat, then String::length. */
    public static BiFunction<String, String, Integer> joinedLength() {
        BiFunction<String, String, String> concat = String::concat;
        return concat.andThen(String::length);
    }

    private static String dashes(String text) {
        return text.replaceAll("\\s", "-");
    }
}
