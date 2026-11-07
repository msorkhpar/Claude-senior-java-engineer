package practice;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Moves customers' birth dates out of an old table. Its three int columns were written with
 * Date.getYear(), Date.getMonth() and Date.getDate().
 */
public class BirthDates {

    /** One row of the old table. */
    public record Row(String customer, int year, int month, int day) {}

    /** The new table's birth dates by customer; rows that name no real date go to {@code rejected}. */
    public static Map<String, LocalDate> migrate(List<Row> rows, List<Row> rejected) {
        Map<String, LocalDate> out = new TreeMap<>();
        for (Row row : rows) {
            try {
                out.put(row.customer(), toLocalDate(row.year(), row.month(), row.day()));
            } catch (DateTimeException e) {
                rejected.add(row);
            }
        }
        return out;
    }

    // TODO: the calendar date the three old columns name. Columns that name no real date throw
    // java.time.DateTimeException, so that migrate() can set the row aside.
    static LocalDate toLocalDate(int year, int month, int day) {
        return LocalDate.of(year, month + 1, day);
    }

    // TODO: the three old columns {year, month, day} for a date, as the old system would have written
    // them; the old reports still read the table during the migration.
    static int[] toColumns(LocalDate date) {
        return new int[] {date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth()};
    }
}
