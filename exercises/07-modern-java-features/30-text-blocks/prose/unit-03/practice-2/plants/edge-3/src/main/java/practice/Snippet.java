package practice;

import java.util.stream.Collectors;

public final class Snippet {

    private Snippet() {
    }

    /** Returns {@code code} with its common indentation replaced by four spaces per level. */
    public static String nest(String code, int level) {
        String stripped = code.stripIndent();
        if (level == 0) {
            return stripped;
        }
        String prefix = " ".repeat(4 * level);
        return stripped.lines()
                .map(line -> line.isEmpty() ? line : prefix + line)
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
