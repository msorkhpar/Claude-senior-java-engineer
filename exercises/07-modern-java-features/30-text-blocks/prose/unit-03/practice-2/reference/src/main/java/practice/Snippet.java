package practice;

import java.util.List;
import java.util.stream.Collectors;

public final class Snippet {

    private Snippet() {
    }

    /** Returns {@code code} with its common indentation replaced by four spaces per level. */
    public static String nest(String code, int level) {
        List<String> lines = code.lines().map(String::stripTrailing).toList();
        int margin = lines.stream()
                .filter(line -> !line.isEmpty())
                .mapToInt(line -> line.length() - line.stripLeading().length())
                .min().orElse(0);
        String prefix = " ".repeat(4 * level);
        return lines.stream()
                .map(line -> line.isEmpty() ? line : prefix + line.substring(margin))
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
