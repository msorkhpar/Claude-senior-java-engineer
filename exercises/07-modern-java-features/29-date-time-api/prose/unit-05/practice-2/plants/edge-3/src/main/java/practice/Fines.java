package practice;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Late fines of a public library. A fine is charged for every day an item is returned after its
 * due date, at a lower rate for members who are not yet 18 on the day they return it.
 */
public class Fines {

    static final long ADULT_CENTS_PER_DAY = 25;
    static final long YOUTH_CENTS_PER_DAY = 10;

    /** The fine in cents for an item due on {@code due}, returned on {@code returned}. */
    public static long fineCents(LocalDate memberBorn, LocalDate due, LocalDate returned) {
        long rate = ageOn(memberBorn, returned) < 18 ? YOUTH_CENTS_PER_DAY : ADULT_CENTS_PER_DAY;
        return daysOverdue(due, returned) * rate;
    }

    /** The number of days {@code returned} is after {@code due}. */
    static long daysOverdue(LocalDate due, LocalDate returned) {
        return ChronoUnit.DAYS.between(due, returned);
    }

    /** The member's age in whole years on {@code day}. */
    static int ageOn(LocalDate born, LocalDate day) {
        return Period.between(born, day).getYears();
    }
}
