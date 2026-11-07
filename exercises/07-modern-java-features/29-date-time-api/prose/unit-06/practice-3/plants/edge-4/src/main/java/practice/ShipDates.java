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
        DateTimeFormatter carrier = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("dd-MMM-uuuu")
                .toFormatter(Locale.US);
        return LocalDate.parse(cell, carrier);
    }

    /** The date as the receipt spells it out. */
    static String receiptDate(LocalDate date) {
        return DateTimeFormatter.ofPattern("EEEE d MMMM uuuu", Locale.ROOT).format(date);
    }
}
