package practice;

import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A pharmacy puts a filled prescription aside for the customer and reminds them every evening
 * until they pick it up. All times are in the pharmacy's own zone.
 */
public class PickupReminders {

    /** The first {@code count} reminders, starting with {@code first}. */
    public static List<ZonedDateTime> reminders(ZonedDateTime first, int count) {
        List<ZonedDateTime> times = new ArrayList<>();
        ZonedDateTime next = first;
        for (int i = 0; i < count; i++) {
            times.add(next);
            next = nextReminder(next);
        }
        return times;
    }

    /** Whether a prescription put aside at {@code heldAt} goes back on the shelf by {@code now}. */
    public static boolean backOnShelf(ZonedDateTime heldAt, ZonedDateTime now) {
        return !now.isBefore(releasedAt(heldAt));
    }

    /** The reminder after {@code last}. */
    static ZonedDateTime nextReminder(ZonedDateTime last) {
        return last.plus(Period.ofDays(1));
    }

    /** When a prescription put aside at {@code heldAt} goes back on the shelf. */
    static ZonedDateTime releasedAt(ZonedDateTime heldAt) {
        return heldAt.plus(Duration.ofHours(24));
    }
}
