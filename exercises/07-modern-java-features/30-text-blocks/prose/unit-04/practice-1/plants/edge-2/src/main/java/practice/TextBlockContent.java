package practice;

public final class TextBlockContent {

    private TextBlockContent() {
    }

    /** Returns the value of a text block whose raw source between the delimiters is {@code raw}. */
    public static String value(String raw) {
        // join continued lines first, then strip and interpret
        return raw.replace("\\\n", "").stripIndent().translateEscapes();
    }
}
