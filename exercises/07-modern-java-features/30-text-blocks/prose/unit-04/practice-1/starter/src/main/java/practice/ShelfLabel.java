package practice;

/**
 * What the shelf-label printer in warehouse 3 receives for one label: a setup command, a line
 * break, then the label's body.
 */
public class ShelfLabel {

    /** The whole print job. */
    public static String job() {
        return setup() + "\n" + body();
    }

    /**
     * The printer's setup command. It must arrive as one line, with no line break of its own.
     */
    static String setup() {
        // TODO: return the setup command from the statement, as a text block.
        throw new IllegalStateException("TODO: setup");
    }

    /**
     * The label's body. The printer reads every line as a field exactly 24 characters wide, and
     * every line, the last one included, ends with a line break.
     */
    static String body() {
        // TODO: return the three body lines from the statement, as a text block.
        throw new IllegalStateException("TODO: body");
    }
}
