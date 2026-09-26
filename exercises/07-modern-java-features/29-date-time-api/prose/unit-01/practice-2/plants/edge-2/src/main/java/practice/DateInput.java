package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateInput {

    // the default SMART resolver moves an out-of-month day to the month's last day
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("uuuu-MM-dd");

    private DateInput() {
    }

    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not a date: " + text, e);
        }
    }
}
