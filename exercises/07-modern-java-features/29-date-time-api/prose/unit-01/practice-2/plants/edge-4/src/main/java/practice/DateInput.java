package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

public final class DateInput {

    // "uuuu" alone also reads a signed or longer year such as +12024
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private DateInput() {
    }

    /** Reads text that is exactly a yyyy-MM-dd date; anything else is an IllegalArgumentException. */
    public static LocalDate parse(String text) {
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not a date: " + text, e);
        }
    }
}
