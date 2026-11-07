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
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length() && text.charAt(i + 1) == '\\') {
                out.append("\\\\");                          // an escaped backslash starts nothing
                i++;
            } else if (c == '\\' && i + 1 < text.length() && text.charAt(i + 1) == 'u') {
                int hex = i + 1;
                while (text.charAt(hex) == 'u') hex++;
                out.append((char) Integer.parseInt(text.substring(hex, hex + 4), 16));
                i = hex + 3;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
