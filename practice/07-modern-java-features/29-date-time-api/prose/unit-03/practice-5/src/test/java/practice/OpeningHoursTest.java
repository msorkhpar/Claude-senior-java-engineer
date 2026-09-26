package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class OpeningHoursTest {

    private static final LocalTime NINE = LocalTime.of(9, 0);
    private static final LocalTime FIVE_PM = LocalTime.of(17, 0);

    @Test
    void insideAndOutsideADayWindow() {
        assertThat(OpeningHours.isOpen(LocalTime.NOON, NINE, FIVE_PM)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.of(8, 59), NINE, FIVE_PM)).isFalse();
        assertThat(OpeningHours.isOpen(LocalTime.of(17, 1), NINE, FIVE_PM)).isFalse();
        assertThat(OpeningHours.isOpen(LocalTime.MIDNIGHT, NINE, FIVE_PM)).isFalse();
        assertThat(OpeningHours.isOpen(LocalTime.of(17, 0, 30), NINE, FIVE_PM)).as("half a minute after closing").isFalse();
        assertThat(OpeningHours.isOpen(LocalTime.NOON, NINE, NINE)).as("open equal to close is not all day").isFalse();
    }

    @Test
    void bothEndsAreOpen() {
        assertThat(OpeningHours.isOpen(LocalTime.parse("09:00"), NINE, FIVE_PM)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.parse("17:00"), NINE, FIVE_PM)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.parse("09:00"), NINE, NINE)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.parse("22:00"), LocalTime.of(22, 0), LocalTime.of(6, 0))).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.parse("06:00"), LocalTime.of(22, 0), LocalTime.of(6, 0))).isTrue();
    }

    @Test
    void anOvernightWindowWrapsMidnight() {
        LocalTime open = LocalTime.of(22, 0);
        LocalTime close = LocalTime.of(6, 0);
        assertThat(OpeningHours.isOpen(LocalTime.of(23, 30), open, close)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.of(2, 0), open, close)).isTrue();
        assertThat(OpeningHours.isOpen(LocalTime.NOON, open, close)).isFalse();
    }
}
