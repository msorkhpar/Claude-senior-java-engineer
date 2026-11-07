package practice;

/**
 * Loads test fixtures that used to be text blocks in the test sources. Each fixture file holds a
 * block's body exactly as it was written: everything after the line of the opening delimiter, up
 * to the closing delimiter.
 */
public class FixtureLoader {

    /** The string javac made of a text block with this body. */
    public static String load(String body) {
        return unicodeEscapes(body).stripIndent().translateEscapes();
    }

    /** The text with every Unicode escape replaced by its character, as javac does it. */
    static String unicodeEscapes(String text) {
        return java.util.regex.Pattern.compile("\\\\u+([0-9a-fA-F]{4})").matcher(text)
                .replaceAll(m -> java.util.regex.Matcher.quoteReplacement(
                        String.valueOf((char) Integer.parseInt(m.group(1), 16))));
    }
}
