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
        // TODO: Split the text into its lines. The export may come from Linux or from Windows,
        //       and may or may not end its last row with a line break.
        throw new IllegalStateException("TODO: linesOf");
    }

    /** The number (from 1) of the first line that differs, or 0 when the two texts match. */
    static int firstDifference(String expected, String actual) {
        // TODO: Compare the two texts line by line. A missing or extra line is a difference too.
        throw new IllegalStateException("TODO: firstDifference");
    }
}
