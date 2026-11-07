package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Part of the team's code-review bot. For every text block a pull request changes, the bot posts
 * the string the compiler will make of it, with its whitespace made visible, because the diff
 * can't show it.
 */
public class TextBlockPreview {

    /**
     * Returns the value javac gives a text block.
     *
     * @param source the block's lines after the opening delimiter's line, up to and including the
     *               line that holds the closing delimiter, exactly as they are in the file. They
     *               contain no escape sequences and no other triple quotes.
     */
    public static String value(List<String> source) {
        String last = source.get(source.size() - 1);
        String beforeClosing = last.substring(0, last.lastIndexOf("\"\"\""));
        boolean closingOnItsOwnLine = beforeClosing.isBlank();
        List<String> content = new ArrayList<>(source.subList(0, source.size() - 1));
        if (!closingOnItsOwnLine) {
            content.add(beforeClosing);
        }
        int margin = margin(content, closingOnItsOwnLine ? beforeClosing : null);
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < content.size(); i++) {
            if (i > 0) {
                value.append('\n');
            }
            value.append(strip(content.get(i), margin));
        }
        if (closingOnItsOwnLine) {
            value.append('\n');
        }
        return value.toString();
    }

    /**
     * How many leading characters the compiler removes from the lines of this block.
     *
     * @param content       the content lines, the last one included when the closing delimiter
     *                      follows it on the same line
     * @param closingIndent the whitespace in front of the closing delimiter when it is on a line of
     *                      its own, or null when it follows the last content line
     */
    static int margin(List<String> content, String closingIndent) {
        int margin = closingIndent == null ? Integer.MAX_VALUE : closingIndent.length();
        for (String line : content) {
            if (!line.isBlank()) {
                margin = Math.min(margin, columns(line));
            }
        }
        return margin == Integer.MAX_VALUE ? 0 : margin;
    }

    /** Leading whitespace in columns, as an editor with a tab width of 4 draws it. */
    private static int columns(String line) {
        int columns = 0;
        for (char c : line.toCharArray()) {
            if (c == '\t') {
                columns += 4;
            } else if (c == ' ') {
                columns++;
            } else {
                break;
            }
        }
        return columns;
    }

    /** One content line as it appears in the value, without its line terminator. */
    static String strip(String line, int margin) {
        return line.isBlank() ? "" : line.substring(margin).stripTrailing();
    }
}
