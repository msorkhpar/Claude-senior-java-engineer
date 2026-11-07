package practice;

import java.util.List;

/** Writes the nightly stock file that the partner's importer reads. */
public final class StockExport {

    /** One stock-keeping unit and the quantity on hand. */
    public record Item(String sku, int qty) { }

    /** The first line of every file. */
    static final String HEADER = "sku,qty";

    private StockExport() {
    }

    /** One record of the file, such as {@code P-0042,7}. */
    static String line(Item item) {
        return item.sku() + "," + item.qty();
    }

    /**
     * The whole file: the header, then one line per item, in the order given, with "\n" between
     * lines.
     */
    public static String csv(List<Item> items) {
        // TODO: The importer reads every line, header included, and catalogues reach a few
        // hundred thousand items.
        throw new IllegalStateException("TODO: csv");
    }
}
