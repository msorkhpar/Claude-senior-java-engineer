package practice;

import java.util.function.Function;

public final class Slugs {

    private Slugs() {
    }

    /** Returns a function turning a title into a lower-case, hyphen-joined slug; null gives "". */
    public static Function<String, String> slugifier() {
        throw new UnsupportedOperationException("write slugifier");
    }
}
