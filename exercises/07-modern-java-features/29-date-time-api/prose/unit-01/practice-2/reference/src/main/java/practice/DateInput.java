package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

public final class DateInput {

    private static final Pattern SHAPE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    // Immutable and thread-safe, so one shared constant is fine
    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private DateInput() {
    }

    /** Reads text that is exactly a yyyy-MM-dd date; anything else is an IllegalArgumentException. */
    public static LocalDate parse(String text) {
        if (!SHAPE.matcher(text).matches()) {
            // exactly four year digits, two month digits, two day digits: no sign, no padding, no spaces
            throw new IllegalArgumentException("not in yyyy-MM-dd form: " + text);
        }
        try {
            return LocalDate.parse(text, FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not a date: " + text, e);
        }
    }
}
