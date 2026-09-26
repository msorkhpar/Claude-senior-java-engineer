package practice;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

public final class Retention {

    private Retention() {
    }

    /** The moment a file created at {@code created} expires, for an ISO-8601 retention amount. */
    public static ZonedDateTime expiresAt(ZonedDateTime created, String amount) {
        try {
            try {
                return created.plus(Duration.parse(amount)); // P1D parses as a Duration of 24 hours
            } catch (DateTimeParseException notADuration) {
                return created.plus(Period.parse(amount));
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("not an ISO-8601 amount: " + amount, e);
        }
    }
}
