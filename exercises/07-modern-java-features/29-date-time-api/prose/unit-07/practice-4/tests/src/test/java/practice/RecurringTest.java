package practice;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RecurringTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");
    private static final ZoneOffset EST = ZoneOffset.ofHours(-5);
    private static final ZoneOffset EDT = ZoneOffset.ofHours(-4);

    private static OffsetDateTime at(int m, int d, int h, int min, ZoneOffset offset) {
        return OffsetDateTime.of(2024, m, d, h, min, 0, 0, offset);
    }

    @Test
    void repeatsWeeklyInWinter() {
        ZonedDateTime first = ZonedDateTime.of(2024, 1, 5, 9, 0, 0, 0, NY);
        assertThat(Recurring.occurrences(first, 3))
                .containsExactly(at(1, 5, 9, 0, EST), at(1, 12, 9, 0, EST), at(1, 19, 9, 0, EST));
        assertThat(Recurring.occurrences(first, 0)).isEmpty();
        ZonedDateTime secondOneThirty = ZonedDateTime.of(2024, 11, 3, 1, 30, 0, 0, NY).withLaterOffsetAtOverlap();
        assertThat(Recurring.occurrences(secondOneThirty, 2))
                .containsExactly(OffsetDateTime.of(2024, 11, 3, 1, 30, 0, 0, EST), at(11, 10, 1, 30, EST));
    }

    @Test
    void theOffsetFollowsDaylightSaving() {
        assertThat(Recurring.occurrences(ZonedDateTime.of(2024, 3, 1, 9, 0, 0, 0, NY), 3))
                .containsExactly(at(3, 1, 9, 0, EST), at(3, 8, 9, 0, EST), at(3, 15, 9, 0, EDT));
        assertThat(Recurring.occurrences(ZonedDateTime.of(2024, 10, 27, 9, 0, 0, 0, NY), 2))
                .containsExactly(at(10, 27, 9, 0, EDT), at(11, 3, 9, 0, EST));
    }

    @Test
    void aSkippedHourDoesNotShiftLaterWeeks() {
        assertThat(Recurring.occurrences(ZonedDateTime.of(2024, 3, 3, 2, 30, 0, 0, NY), 3))
                .containsExactly(at(3, 3, 2, 30, EST), at(3, 10, 3, 30, EDT), at(3, 17, 2, 30, EDT));
    }
}
