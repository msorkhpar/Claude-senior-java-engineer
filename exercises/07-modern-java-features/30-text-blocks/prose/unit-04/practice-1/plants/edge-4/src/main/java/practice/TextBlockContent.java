package practice;

public final class TextBlockContent {

    private TextBlockContent() {
    }

    /** Returns the value of a text block whose raw source between the delimiters is {@code raw}. */
    public static String value(String raw) {
        String value = raw.stripIndent().translateEscapes();
        return value.replace("\r\n", "\n").replace('\r', '\n');
    }
}
