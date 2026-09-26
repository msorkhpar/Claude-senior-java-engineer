package practice;

import java.util.function.Function;

public final class Slugs {

    private Slugs() {
    }

    public static Function<String, String> slugifier() {
        Function<String, String> nullToEmpty = s -> s == null ? "" : s;
        Function<String, String> lower = String::toLowerCase;
        Function<String, String> remove = s -> s.replaceAll("[^\\w\\s-]", "");
        Function<String, String> trim = String::trim;
        Function<String, String> collapse = s -> s.replaceAll("\\s+", " ");
        Function<String, String> hyphen = s -> s.replace(" ", "-");
        return nullToEmpty.andThen(lower).andThen(remove).andThen(trim).andThen(collapse).andThen(hyphen);
    }
}
