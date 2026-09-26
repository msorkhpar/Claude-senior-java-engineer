package practice;

public final class TextBlockContent {

    private TextBlockContent() {
    }

    /** Returns the value of a text block whose raw source between the delimiters is {@code raw}. */
    public static String value(String raw) {
        return raw.translateEscapes().stripIndent();
    }
}
