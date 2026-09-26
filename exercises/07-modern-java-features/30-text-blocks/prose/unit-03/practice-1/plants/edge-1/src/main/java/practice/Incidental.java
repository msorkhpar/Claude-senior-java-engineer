package practice;

import java.util.List;

public final class Incidental {

    private Incidental() {
    }

    /** Returns how many leading whitespace characters of the text block are incidental. */
    public static int width(List<String> lines, String closing) {
        int width = Integer.MAX_VALUE;
        for (String line : lines) {
            if (!line.isBlank() || line.isEmpty()) {
                width = Math.min(width, leading(line));
            }
        }
        if (closing != null) {
            width = Math.min(width, closing.length());
        }
        return width == Integer.MAX_VALUE ? 0 : width;
    }

    private static int leading(String line) {
        int n = 0;
        while (n < line.length() && Character.isWhitespace(line.charAt(n))) {
            n++;
        }
        return n;
    }
}
