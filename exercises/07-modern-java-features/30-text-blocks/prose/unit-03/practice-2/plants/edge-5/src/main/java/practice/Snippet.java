package practice;

import java.util.stream.Collectors;

public final class Snippet {

    private Snippet() {
    }

    /** Returns {@code code} with its common indentation replaced by four spaces per level. */
    public static String nest(String code, int level) {
        String[] lines = code.split("\r\n|\r|\n", -1);
        int margin = Integer.MAX_VALUE;
        for (String line : lines) {
            if (!line.isBlank()) {
                margin = Math.min(margin, line.length() - line.stripLeading().length());
            }
        }
        String prefix = " ".repeat(4 * level);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].isBlank() ? "" : lines[i].substring(margin);
            if (i == lines.length - 1 && line.isEmpty()) {
                break;
            }
            out.append(line.isEmpty() ? "" : prefix + line).append('\n');
        }
        return out.toString();
    }
}
