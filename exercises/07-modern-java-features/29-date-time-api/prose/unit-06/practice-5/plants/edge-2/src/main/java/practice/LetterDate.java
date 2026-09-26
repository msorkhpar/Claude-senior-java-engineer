package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public final class LetterDate {

    private LetterDate() {
    }

    /** {@code date} in the reader's own full-date style. */
    public static String format(LocalDate date, Locale reader) {
        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
    }
}
