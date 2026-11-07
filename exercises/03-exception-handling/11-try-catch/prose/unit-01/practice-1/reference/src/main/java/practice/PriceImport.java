package practice;

import java.util.List;
import java.util.Map;

/**
 * Imports a supplier's price list into the catalog. Each row maps a column name from the file's
 * header to the cell's text. Runs as a job that a deploy stops by interrupting its thread.
 */
public class PriceImport {

    /** Limits the write rate to the catalog database. In production it blocks until a slot is free. */
    public interface Throttle {
        void acquire() throws InterruptedException;
    }

    /** The catalog database. */
    public interface Catalog {
        void put(String sku, long cents);
    }

    /** How many rows went into the catalog, and how many were turned away as unreadable. */
    public record Result(int imported, int rejected) {
    }

    private final Throttle throttle;
    private final Catalog catalog;

    public PriceImport(Throttle throttle, Catalog catalog) {
        this.throttle = throttle;
        this.catalog = catalog;
    }

    /** Imports every row in order and reports the counts. */
    public Result importAll(List<Map<String, String>> rows) throws InterruptedException {
        int imported = 0;
        int rejected = 0;
        for (Map<String, String> row : rows) {
            if (importRow(row)) {
                imported++;
            } else {
                rejected++;
            }
        }
        return new Result(imported, rejected);
    }

    /**
     * Imports one row: its "price_cents" cell, a whole number of cents that may have spaces around
     * it, becomes the price of its "sku". Returns false for a row whose price isn't a number.
     */
    boolean importRow(Map<String, String> row) throws InterruptedException {
        long cents;
        try {
            cents = Long.parseLong(row.get("price_cents").strip());
        } catch (NumberFormatException e) {
            return false;
        }
        throttle.acquire();
        catalog.put(row.get("sku"), cents);
        return true;
    }
}
