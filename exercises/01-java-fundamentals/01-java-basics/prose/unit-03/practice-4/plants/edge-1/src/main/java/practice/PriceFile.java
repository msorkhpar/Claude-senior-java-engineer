package practice;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Writes the nightly price file that every store downloads, and reads it back. One line per
 * product, {@code sku;price}, the price with two decimals. Stores sometimes edit the file by hand
 * before importing it again.
 */
public class PriceFile {

    /** The file's text, one line per product, in the map's order. */
    public String write(Map<String, Double> prices) {
        StringBuilder text = new StringBuilder();
        prices.forEach((sku, price) -> text.append(sku).append(';').append(formatPrice(price)).append('\n'));
        return text.toString();
    }

    /** The prices in the file's text, in file order. */
    public Map<String, Double> read(String text) {
        Map<String, Double> prices = new LinkedHashMap<>();
        for (String line : text.split("\n")) {
            if (line.isBlank()) continue;
            String[] fields = line.split(";", 2);
            prices.put(fields[0], parsePrice(fields[1]));
        }
        return prices;
    }

    /** The price as the file writes it, with two decimals. */
    String formatPrice(double price) {
        // TODO: Every store, wherever its server runs, must be able to read the file back.
        return String.format("%.2f", price);
    }

    /** The price a field of the file holds. */
    double parsePrice(String field) {
        // TODO: Return the price. A field that doesn't hold a usable price must be refused
        //       with an IllegalArgumentException, so a bad file never reaches the tills.
        double price = Double.parseDouble(field);
        if (!Double.isFinite(price)) throw new IllegalArgumentException("not a price: " + field);
        return price;
    }
}
