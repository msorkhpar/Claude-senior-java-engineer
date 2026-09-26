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
        int years = on.getYear() - birth.getYear();
        // compares positions in the year, which a leap day shifts
        return on.getDayOfYear() < birth.getDayOfYear() ? years - 1 : years;
    }

    /** The age on the date the clock shows in its own zone. */
    public static int ageToday(LocalDate birth, Clock clock) {
        return ageOn(birth, LocalDate.now(clock));
    }
}
