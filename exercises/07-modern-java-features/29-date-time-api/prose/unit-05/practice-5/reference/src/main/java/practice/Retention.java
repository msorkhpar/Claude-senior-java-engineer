package practice;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public final class Retention {

    private Retention() {
    }

    /** The moment a file created at {@code created} expires, for an ISO-8601 retention amount. */
    public static ZonedDateTime expiresAt(ZonedDateTime created, String amount) {
        try {
            if (amount.toUpperCase(Locale.ROOT).contains("T")) {
                return created.plus(Duration.parse(amount)); // exact elapsed time
            }
            return created.plus(Period.parse(amount)); // calendar amount
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not an ISO-8601 amount: " + amount, e);
        }
    }
}
