package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetTest {

    private static Timesheet.Shift shift(int h1, int m1, int h2, int m2) {
        return new Timesheet.Shift(LocalTime.of(h1, m1), LocalTime.of(h2, m2));
    }

    @Test
    void showsTheOvertimeOfAWeekOfDayShifts() {
        List<Timesheet.Shift> week = Collections.nCopies(5, shift(7, 45, 16, 15));

        assertThat(Timesheet.balance(week, Duration.ofHours(40))).isEqualTo("2:30");
        assertThat(Timesheet.balance(List.of(shift(9, 0, 9, 50)), Duration.ZERO)).isEqualTo("0:50");
    }

    @Test
    void aShiftThatEndsAfterMidnightCountsItsHoursAfterMidnight() {
        assertThat(Timesheet.length(LocalTime.of(21, 45), LocalTime.of(5, 15))).isEqualTo(Duration.ofMinutes(450));
        assertThat(Timesheet.length(LocalTime.of(18, 0), LocalTime.MIDNIGHT)).isEqualTo(Duration.ofHours(6));
        assertThat(Timesheet.balance(Collections.nCopies(4, shift(21, 45, 5, 15)), Duration.ofHours(32))).isEqualTo("-2:00");
    }

    @Test
    void aBalanceOfMoreThanADayShowsAllItsHours() {
        assertThat(Timesheet.label(Duration.ofHours(27).plusMinutes(5))).isEqualTo("27:05");
        assertThat(Timesheet.balance(Collections.nCopies(6, shift(6, 0, 18, 0)), Duration.ofHours(40))).isEqualTo("32:00");
    }

    @Test
    void aNegativeBalanceShowsOneMinusSign() {
        assertThat(Timesheet.label(Duration.ofMinutes(-45))).isEqualTo("-0:45");
        assertThat(Timesheet.label(Duration.ofMinutes(-135))).isEqualTo("-2:15");
        assertThat(Timesheet.balance(List.of(shift(8, 0, 12, 20)), Duration.ofHours(5))).isEqualTo("-0:40");
    }
}
