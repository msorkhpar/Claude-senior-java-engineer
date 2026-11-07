package practice;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PickupRemindersTest {

    private static final ZoneId CHICAGO = ZoneId.of("America/Chicago");
    private static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    private static List<String> text(List<ZonedDateTime> times) {
        return times.stream().map(ZonedDateTime::toString).toList();
    }

    @Test
    void remindsAtTheSameTimeEachEveningAndReleasesAfterADay() {
        ZonedDateTime first = ZonedDateTime.of(2026, 6, 10, 18, 30, 0, 0, CHICAGO);

        assertThat(text(PickupReminders.reminders(first, 3))).containsExactly(
                "2026-06-10T18:30-05:00[America/Chicago]",
                "2026-06-11T18:30-05:00[America/Chicago]",
                "2026-06-12T18:30-05:00[America/Chicago]");
        assertThat(PickupReminders.releasedAt(first).toString()).isEqualTo("2026-06-11T18:30-05:00[America/Chicago]");
        assertThat(PickupReminders.backOnShelf(first, first.plusHours(23).plusMinutes(59))).isFalse();
        assertThat(PickupReminders.backOnShelf(first, first.plusHours(24))).isTrue();
    }

    @Test
    void remindersKeepTheirEveningTimeWhenTheClocksGoForward() {
        // America/Chicago moves its clocks from 02:00 to 03:00 on 2026-03-08.
        ZonedDateTime first = ZonedDateTime.of(2026, 3, 6, 19, 15, 0, 0, CHICAGO);

        assertThat(text(PickupReminders.reminders(first, 4))).containsExactly(
                "2026-03-06T19:15-06:00[America/Chicago]",
                "2026-03-07T19:15-06:00[America/Chicago]",
                "2026-03-08T19:15-05:00[America/Chicago]",
                "2026-03-09T19:15-05:00[America/Chicago]");
    }

    @Test
    void aHoldLastsTwentyFourHoursWhenTheClocksGoBack() {
        // Australia/Sydney moves its clocks from 03:00 back to 02:00 on 2026-04-05.
        ZonedDateTime heldAt = ZonedDateTime.of(2026, 4, 4, 11, 40, 0, 0, SYDNEY);

        assertThat(PickupReminders.releasedAt(heldAt).toString()).isEqualTo("2026-04-05T10:40+10:00[Australia/Sydney]");
        assertThat(PickupReminders.backOnShelf(heldAt, ZonedDateTime.of(2026, 4, 5, 10, 45, 0, 0, SYDNEY))).isTrue();
    }
}
