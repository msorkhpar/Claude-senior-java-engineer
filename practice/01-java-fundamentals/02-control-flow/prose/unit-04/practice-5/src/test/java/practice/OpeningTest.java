package practice;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;

import static org.assertj.core.api.Assertions.*;

class OpeningTest {

    @Test
    void opensOnWeekdays() {
        assertThat(Opening.hours(DayOfWeek.MONDAY)).isEqualTo(9);
        assertThat(Opening.hours(DayOfWeek.WEDNESDAY)).isEqualTo(9);
        assertThat(Opening.hours(DayOfWeek.FRIDAY)).isEqualTo(9);
    }

    @Test
    void saturdayIsShort() {
        assertThat(Opening.hours(DayOfWeek.SATURDAY)).isEqualTo(5);
    }

    @Test
    void sundayIsClosed() {
        assertThat(Opening.hours(DayOfWeek.SUNDAY)).isZero();
    }
}
