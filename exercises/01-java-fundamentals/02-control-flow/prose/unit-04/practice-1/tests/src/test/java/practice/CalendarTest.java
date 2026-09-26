package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CalendarTest {

    @Test
    void countsTheDaysOfEachMonth() {
        int[] expected = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        for (int month = 1; month <= 12; month++) {
            assertThat(Calendar.days(month, 2023)).as("month %d", month).isEqualTo(expected[month - 1]);
        }
    }

    @Test
    void februaryFollowsTheLeapYearRule() {
        assertThat(Calendar.days(2, 2024)).isEqualTo(29);
        assertThat(Calendar.days(2, 2000)).isEqualTo(29);
        assertThat(Calendar.days(2, 1900)).isEqualTo(28);
        assertThat(Calendar.days(2, 2100)).isEqualTo(28);
    }

    @Test
    void anUnknownMonthIsMinusOne() {
        assertThat(Calendar.days(13, 2023)).isEqualTo(-1);
        assertThat(Calendar.days(0, 2023)).isEqualTo(-1);
        assertThat(Calendar.days(-1, 2023)).isEqualTo(-1);
    }
}
