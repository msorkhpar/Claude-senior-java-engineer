package practice;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RemindersTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");

    private static ZonedDateTime ny(int y, int m, int d, int h, int min) {
        return ZonedDateTime.of(y, m, d, h, min, 0, 0, NY);
    }

    private static ZonedDateTime ny(int y, int m, int d, int h) {
        return ny(y, m, d, h, 0);
    }

    /** AssertJ compares ZonedDateTimes by instant; the text also pins the wall clock, offset and zone. */
    private static void same(ZonedDateTime actual, ZonedDateTime expected) {
        assertThat(actual.toString()).isEqualTo(expected.toString());
    }

    @Test
    void anOrdinaryDayIs24Hours() {
        same(Reminders.sameTimeTomorrow(ny(2024, 3, 15, 9)), ny(2024, 3, 16, 9));
        same(Reminders.exactlyADayLater(ny(2024, 3, 15, 9)), ny(2024, 3, 16, 9));
        assertThat(Reminders.hoursBetween(ny(2024, 3, 15, 9), ny(2024, 3, 16, 9))).isEqualTo(24);
        assertThat(Reminders.hoursBetween(ny(2024, 3, 16, 9), ny(2024, 3, 15, 9))).isEqualTo(-24);
        assertThat(Reminders.hoursBetween(ny(2024, 7, 1, 8), ny(2024, 7, 1, 17))).isEqualTo(9);
        assertThat(Reminders.hoursBetween(ny(2024, 7, 1, 9), ny(2024, 7, 1, 10, 40))).isEqualTo(1);
        assertThat(Reminders.hoursBetween(ny(2024, 7, 1, 10, 30), ny(2024, 7, 1, 9))).isEqualTo(-1);
    }

    @Test
    void aCalendarDayKeepsTheWallClock() {
        same(Reminders.sameTimeTomorrow(ny(2024, 3, 10, 0)), ny(2024, 3, 11, 0));
        same(Reminders.sameTimeTomorrow(ny(2024, 11, 3, 0)), ny(2024, 11, 4, 0));
    }

    @Test
    void anExactDayKeepsTheHours() {
        same(Reminders.exactlyADayLater(ny(2024, 3, 10, 0)), ny(2024, 3, 11, 1));
        same(Reminders.exactlyADayLater(ny(2024, 11, 3, 0)), ny(2024, 11, 3, 23));
    }

    @Test
    void elapsedHoursFollowTheRealClock() {
        assertThat(Reminders.hoursBetween(ny(2024, 3, 10, 0), ny(2024, 3, 11, 0))).isEqualTo(23);
        assertThat(Reminders.hoursBetween(ny(2024, 11, 3, 0), ny(2024, 11, 4, 0))).isEqualTo(25);
    }
}
