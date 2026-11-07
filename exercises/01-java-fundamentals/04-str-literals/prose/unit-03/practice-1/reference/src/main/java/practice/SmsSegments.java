package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a message into SMS segments. A Unicode SMS segment holds a fixed number of UTF-16
 * code units (70 for a single message), and the phone joins the segments back together.
 */
public class SmsSegments {

    /** Splits {@code text} into segments of at most {@code units} UTF-16 code units each. */
    public static List<String> segments(String text, int units) {
        if (units < 2) throw new IllegalArgumentException("a segment holds at least 2 units");
        List<String> result = new ArrayList<>();
        String rest = text;
        while (!rest.isEmpty()) {
            int end = cut(rest, units);
            result.add(rest.substring(0, end));
            rest = rest.substring(end);
        }
        return result;
    }

    /** Where the first segment of {@code text} ends: an index into {@code text}. */
    static int cut(String text, int units) {
        if (text.length() <= units) return text.length();
        int end = units;
        if (Character.isHighSurrogate(text.charAt(end - 1))) end--;
        return end;
    }
}
