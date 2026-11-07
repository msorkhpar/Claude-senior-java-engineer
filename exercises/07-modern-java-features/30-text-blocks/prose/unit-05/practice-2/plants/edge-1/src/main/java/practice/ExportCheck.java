package practice;

import java.util.List;

/**
 * Checks the nightly CSV export against an expected file kept as a text block in the tests.
 * The export runs on Linux and on Windows hosts, and the partner's parser accepts either line
 * ending and either way of ending the last line.
 */
public class ExportCheck {

    /** A one-line verdict for the build log. */
    public static String verdict(String expected, String actual) {
        int line = firstDifference(expected, actual);
        if (line == 0) return "export matches";
        List<String> want = linesOf(expected);
        List<String> got = linesOf(actual);
        return "line %d differs: expected [%s], got [%s]".formatted(line,
                line <= want.size() ? want.get(line - 1) : "<none>",
                line <= got.size() ? got.get(line - 1) : "<none>");
    }

    /** The lines of a text, without their line terminators. */
    static List<String> linesOf(String text) {
        return List.of(text.split("\n"));
    }

    /** The number (from 1) of the first line that differs, or 0 when the two texts match. */
    static int firstDifference(String expected, String actual) {
        List<String> want = linesOf(expected);
        List<String> got = linesOf(actual);
        for (int i = 0; i < Math.max(want.size(), got.size()); i++) {
            if (i >= want.size() || i >= got.size() || !want.get(i).equals(got.get(i))) {
                return i + 1;
            }
        }
        return 0;
    }
}
