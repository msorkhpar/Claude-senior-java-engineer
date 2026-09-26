package practice;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class UserDates {

    private static final List<String> PATTERNS = List.of("uuuu-MM-dd", "MM/dd/uuuu", "dd-MMM-uuuu");

    private UserDates() {
    }

    /** The date {@code text} names in one of the accepted forms, or empty. */
    public static Optional<LocalDate> parse(String text) {
        if (text == null) {
            return Optional.empty();
        }
        for (String p : PATTERNS) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern(p, Locale.US).withResolverStyle(ResolverStyle.STRICT);
            try {
                return Optional.of(LocalDate.parse(text, f));
            } catch (DateTimeParseException notThisForm) {
                // try the next form
            }
        }
        return Optional.empty();
    }
}
