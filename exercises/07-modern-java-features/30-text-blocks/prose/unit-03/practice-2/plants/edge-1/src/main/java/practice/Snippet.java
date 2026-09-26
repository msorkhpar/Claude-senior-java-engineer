package practice;

import java.util.stream.Collectors;

public final class Snippet {

    private Snippet() {
    }

    /** Returns {@code code} with its common indentation replaced by four spaces per level. */
    public static String nest(String code, int level) {
        return code.stripIndent().indent(4 * level);
    }
}
