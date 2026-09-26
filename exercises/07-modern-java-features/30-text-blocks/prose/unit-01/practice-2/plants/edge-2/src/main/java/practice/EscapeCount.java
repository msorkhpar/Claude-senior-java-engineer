package practice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EscapeCount {

    private static final Pattern PAIR = Pattern.compile("\\\\\\\\");
    private static final Pattern LETTER = Pattern.compile("\\\\[ntrbfs\"']");

    private EscapeCount() {
    }

    /** Returns how many escape sequences the literal body {@code body} holds. */
    public static int count(String body) {
        // counts the doubled backslashes, then every backslash followed by an escape letter
        int escapes = 0;
        Matcher pairs = PAIR.matcher(body);
        while (pairs.find()) {
            escapes++;
        }
        Matcher letters = LETTER.matcher(body);
        while (letters.find()) {
            escapes++;
        }
        return escapes;
    }
}
