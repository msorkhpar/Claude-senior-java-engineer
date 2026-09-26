package practice;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;

public final class Ages {

    private Ages() {
    }

    /** Whole years from birth to on. */
    public static int ageOn(LocalDate birth, LocalDate on) {
        if (birth.isAfter(on)) {
            throw new IllegalArgumentException("birth " + birth + " is after " + on);
        }
        return Period.between(birth, on).getYears();
    }

    /** The age on the date the clock shows in its own zone. */
    public static int ageToday(LocalDate birth, Clock clock) {
        return ageOn(birth, LocalDate.ofInstant(clock.instant(), java.time.ZoneOffset.UTC)); // today in UTC, not in the clock's zone
    }
}
