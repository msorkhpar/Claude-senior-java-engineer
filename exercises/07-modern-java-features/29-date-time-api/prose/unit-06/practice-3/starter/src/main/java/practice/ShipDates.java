package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

/**
 * Reads the ship-date column of a carrier's CSV export and writes the date line on the
 * customer's receipt.
 */
public class ShipDates {

    /** One receipt line for one CSV cell. */
    public static String receiptFor(String cell) {
        return "Shipped " + receiptDate(shipped(cell));
    }

    /** The date in one cell of the carrier's ship-date column. */
    static LocalDate shipped(String cell) {
        // TODO: read a cell of the carrier's column (see the statement for what the carrier writes).
        throw new IllegalStateException("TODO: shipped(cell)");
    }

    /** The date as the receipt spells it out. */
    static String receiptDate(LocalDate date) {
        // TODO: spell the date out the way the receipt shows it (see the statement).
        throw new IllegalStateException("TODO: receiptDate(date)");
    }
}
