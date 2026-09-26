package practice;

public final class TextBlockSource {

    private static final String DELIMITER = "\"\"\"";
    private static final String INDENT = "        ";

    private TextBlockSource() {
    }

    /** Returns the Java source of a text block whose value is {@code value}. */
    public static String of(String value) {
        String[] lines = value.split("\n", -1);
        StringBuilder source = new StringBuilder(DELIMITER).append('\n');
        for (int i = 0; i < lines.length; i++) {
            boolean last = i == lines.length - 1;
            String line = escape(lines[i], last);
            if (!line.isEmpty() || last) {
                source.append(INDENT);
            }
            source.append(line).append(last ? DELIMITER : "\n");
        }
        return source.toString();
    }

    private static String escape(String line, boolean closingFollows) {
        StringBuilder out = new StringBuilder();
        int quotes = 0;
        boolean endsWithEscapedQuote = false;
        for (char c : line.toCharArray()) {
            endsWithEscapedQuote = false;
            if (c == '"') {
                quotes++;
                if (quotes == 3) {
                    out.append("\\\"");
                    quotes = 0;
                    endsWithEscapedQuote = true;
                } else {
                    out.append('"');
                }
                continue;
            }
            quotes = 0;
            if (c == '\\') {
                out.append("\\\\");
            } else {
                out.append(c);
            }
        }
        int end = out.length() - 1;
        if (end >= 0 && out.charAt(end) == ' ') {
            out.replace(end, end + 1, "\\s");
        } else if (closingFollows && end >= 0 && out.charAt(end) == '"' && !endsWithEscapedQuote) {
            out.replace(end, end + 1, "\\\"");
        }
        return out.toString();
    }
}
