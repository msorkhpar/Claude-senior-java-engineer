package practice;

import java.util.function.Function;

public final class Slugs {

    private Slugs() {
    }

    /** Returns a function turning a title into a lower-case, hyphen-joined slug; null gives "". */
    public static Function<String, String> slugifier() {
        Function<String, String> nullToEmpty = s -> s == null ? "" : s;
        Function<String, String> lower = s -> s.toLowerCase(java.util.Locale.ROOT);
        Function<String, String> removeSpecialChars = s -> s.replaceAll("[^a-z0-9\\s-]", "");
        Function<String, String> trim = String::trim;
        Function<String, String> collapseSpaces = s -> s.replaceAll("\\s+", " ");
        Function<String, String> spaceToHyphen = s -> s.replace(" ", "-");
        return nullToEmpty
                .andThen(lower)
                .andThen(removeSpecialChars)
                .andThen(trim)
                .andThen(collapseSpaces)
                .andThen(spaceToHyphen);
    }
}
