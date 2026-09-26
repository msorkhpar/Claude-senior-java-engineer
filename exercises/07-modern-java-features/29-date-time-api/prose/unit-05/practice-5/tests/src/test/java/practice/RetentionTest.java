package practice;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetentionTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");

    private static ZonedDateTime ny(int y, int m, int d, int h, int min) {
        return ZonedDateTime.of(y, m, d, h, min, 0, 0, NY);
    }

    /** AssertJ compares ZonedDateTimes by instant; the text also pins the wall clock, offset and zone. */
    private static void same(ZonedDateTime actual, ZonedDateTime expected) {
        assertThat(actual.toString()).isEqualTo(expected.toString());
    }

    @Test
    void addsCalendarAndClockAmounts() {
        ZonedDateTime created = ny(2024, 1, 15, 9, 0);
        same(Retention.expiresAt(created, "P6M"), ny(2024, 7, 15, 9, 0));
        same(Retention.expiresAt(created, "PT90M"), ny(2024, 1, 15, 10, 30));
        same(Retention.expiresAt(created, "P1Y2M3D"), ny(2025, 3, 18, 9, 0));
        same(Retention.expiresAt(created, "PT2H30M"), ny(2024, 1, 15, 11, 30));
        same(Retention.expiresAt(created, "P1M"), ny(2024, 2, 15, 9, 0));
        same(Retention.expiresAt(created, "PT1M"), ny(2024, 1, 15, 9, 1));
        same(Retention.expiresAt(created, "P1DT12H"), ny(2024, 1, 16, 21, 0));
    }

    @Test
    void aDayIsACalendarDay() {
        ZonedDateTime created = ny(2024, 3, 9, 12, 0);
        same(Retention.expiresAt(created, "P1D"), ny(2024, 3, 10, 12, 0));
        same(Retention.expiresAt(created, "PT24H"), ny(2024, 3, 10, 13, 0));
    }

    @Test
    void textThatIsNoAmountIsRefused() {
        ZonedDateTime created = ny(2024, 1, 15, 9, 0);
        assertThatThrownBy(() -> Retention.expiresAt(created, "6 months")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Retention.expiresAt(created, "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Retention.expiresAt(created, "PT")).isInstanceOf(IllegalArgumentException.class);
    }
}
