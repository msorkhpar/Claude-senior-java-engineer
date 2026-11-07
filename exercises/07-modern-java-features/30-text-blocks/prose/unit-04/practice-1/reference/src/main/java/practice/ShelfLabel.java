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
        return """
                ^XA^PW406^LL203^LH0,0^MD10^PR4\
                ^FO20,20^A0N,28,28\
                ^FDAISLE B^FS^XZ""";
    }

    /**
     * The label's body. The printer reads every line as a field exactly 24 characters wide, and
     * every line, the last one included, ends with a line break.
     */
    static String body() {
        return """
                AISLE B / SHELF 07     \s
                SKU 44-1093            \s
                QTY 12                 \s
                """;
    }
}
