package practice;

import java.util.stream.Collectors;

public final class Snippet {

    private Snippet() {
    }

    /** Returns {@code code} with its common indentation replaced by four spaces per level. */
    public static String nest(String code, int level) {
        String prefix = " ".repeat(4 * level);
        java.util.List<String> ls = code.lines().toList();
        int min = ls.stream().filter(l -> !l.isEmpty()).mapToInt(l -> l.length() - l.stripLeading().length()).min().orElse(0);
        return ls.stream().map(l -> l.isBlank() ? "" : prefix + l.substring(Math.min(min, l.length())).stripTrailing())
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
