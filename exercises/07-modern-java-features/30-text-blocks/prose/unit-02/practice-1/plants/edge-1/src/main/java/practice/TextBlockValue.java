package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TextBlockValue {

    private TextBlockValue() {
    }

    /** Returns the value of the text block whose source after the opening delimiter is {@code body}. */
    public static String evaluate(String body) {
        String[] lines = body.split("\r\n|\r|\n", -1);
        String last = lines[lines.length - 1];
        String beforeClosing = last.substring(0, last.length() - 3);
        boolean ownLine = beforeClosing.isBlank();
        List<String> content = new ArrayList<>(Arrays.asList(lines).subList(0, lines.length - 1));
        if (!ownLine) {
            content.add(beforeClosing);
        }
        int margin = Integer.MAX_VALUE;
        for (String line : content) {
            margin = Math.min(margin, leadingSpaces(line));
        }
        if (ownLine) {
            margin = Math.min(margin, beforeClosing.length());
        }
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < content.size(); i++) {
            if (i > 0) {
                value.append('\n');
            }
            value.append(content.get(i).substring(margin));
        }
        return value.toString();
    }

    private static int leadingSpaces(String line) {
        int n = 0;
        while (n < line.length() && line.charAt(n) == ' ') {
            n++;
        }
        return n;
    }
}
