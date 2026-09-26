package practice;

public final class TextBlockContent {

    private TextBlockContent() {
    }

    /** Returns the value of a text block whose raw source between the delimiters is {@code raw}. */
    public static String value(String raw) {
        // a backslash before a line break joins the lines, with the next line's indent
        return raw.replaceAll("\\\\\n[ \t]*", "").stripIndent().translateEscapes();
    }
}
