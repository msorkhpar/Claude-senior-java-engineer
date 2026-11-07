package practice;

import java.util.List;

/**
 * Writes the nightly price list: one line per SKU, "sku;price", in catalogue order. Pricing one
 * SKU takes a lot of CPU, so the export has to price several SKUs at once to finish in time.
 */
public class PriceExport {

    /** Works out one SKU's price. CPU-bound, and safe to call from several threads at once. */
    public interface Pricer {
        String price(String sku);
    }

    /** Appends one line to the export file. Not thread-safe, and the file must follow the catalogue. */
    public interface LineWriter {
        void write(String line);
    }

    private final Pricer pricer;
    private final LineWriter out;

    public PriceExport(Pricer pricer, LineWriter out) {
        this.pricer = pricer;
        this.out = out;
    }

    /** Prices every SKU of the catalogue and writes its line. */
    public void export(List<String> catalogue) {
        catalogue.parallelStream().map(this::line).forEach(out::write);
    }

    /** The line for one SKU. */
    String line(String sku) {
        return sku + ";" + pricer.price(sku);
    }
}
