package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads a supplier's price list, one {@code sku;name;currency;country} line per product, into a
 * catalogue that stays in memory until the next price list replaces it.
 */
public class CatalogueLoader {

    public record Product(String sku, String name, String currency, String country) { }

    private final Map<String, String> codes = new HashMap<>();

    public List<Product> load(List<String> lines) {
        List<Product> products = new ArrayList<>(lines.size());
        for (String line : lines) {
            products.add(parse(line));
        }
        return products;
    }

    /** How many strings this loader's table of shared values holds. */
    public int tableSize() {
        return codes.size();
    }

    /** One line of the price list as a product. */
    Product parse(String line) {
        String[] f = line.split(";");
        return new Product(f[0], f[1], shared(f[2]), shared(f[3]));
    }

    /** The one string this loader keeps for {@code value}'s text. */
    String shared(String value) {
        String kept = codes.putIfAbsent(value, value);
        return kept == null ? value : kept;
    }
}
