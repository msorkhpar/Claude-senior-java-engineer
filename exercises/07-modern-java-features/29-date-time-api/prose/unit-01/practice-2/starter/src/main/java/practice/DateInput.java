package practice;

import java.time.LocalDate;

public final class DateInput {

    private DateInput() {
    }

    /** Reads text that is exactly a yyyy-MM-dd date; anything else is an IllegalArgumentException. */
    public static LocalDate parse(String text) {
        throw new UnsupportedOperationException("write parse");
    }
}
