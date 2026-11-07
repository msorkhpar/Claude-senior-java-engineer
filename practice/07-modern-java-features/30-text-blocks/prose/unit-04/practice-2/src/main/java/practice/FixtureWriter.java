package practice;

/**
 * Part of a test generator: it records what a service returned and writes it into the generated
 * test as a Java text block, so that the test compares the next run with exactly that string.
 */
public class FixtureWriter {

    private static final String INDENT = " ".repeat(8);

    /**
     * Java source for a text block whose value is {@code value}. The value holds printable ASCII
     * characters, spaces, carriage returns and line feeds.
     */
    public static String textBlock(String value) {
        StringBuilder source = new StringBuilder("\"\"\"\n");
        String[] lines = value.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            source.append(INDENT).append(line(lines[i]));
            if (i < lines.length - 1) source.append('\n');
        }
        // The last line ends with a backslash, so the closing delimiter can sit on a line of its
        // own, fixing the indentation, without adding a line break to the value.
        return source.append("\\\n").append(INDENT).append("\"\"\"").toString();
    }

    /**
     * One line of the value (it holds no line feed) as it must be written inside the text block,
     * between the indentation and the line break.
     */
    static String line(String text) {
        // TODO: Return what to write so that javac reads back exactly this text. Change only
        //       what would otherwise compile to something else, or not compile at all.
        throw new IllegalStateException("TODO: line");
    }
}
