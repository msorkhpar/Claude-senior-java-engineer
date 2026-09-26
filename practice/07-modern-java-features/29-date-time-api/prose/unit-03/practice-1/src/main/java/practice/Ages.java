package practice;

import java.time.Clock;
import java.time.LocalDate;

public final class Ages {

    private Ages() {
    }

    /** Whole years from birth to on. */
    public static int ageOn(LocalDate birth, LocalDate on) {
        throw new UnsupportedOperationException("write ageOn");
    }

    /** The age on the date the clock shows in its own zone. */
    public static int ageToday(LocalDate birth, Clock clock) {
        throw new UnsupportedOperationException("write ageToday");
    }
}
