package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetTest {

    private static Timesheet.Shift shift(String start, String end) {
        return new Timesheet.Shift(LocalTime.parse(start), LocalTime.parse(end));
    }

    @Test
    void addsUpTheShifts() {
        assertThat(Timesheet.total(List.of(shift("09:00", "12:30"), shift("13:00", "17:15")))).isEqualTo("7:45");
        assertThat(Timesheet.total(List.of())).isEqualTo("0:00");
        assertThat(Timesheet.total(List.of(shift("08:05", "08:10")))).isEqualTo("0:05");
        assertThat(Timesheet.total(List.of(shift("08:00", "18:00"), shift("08:00", "10:10")))).isEqualTo("12:10");
    }

    @Test
    void anOvernightShiftCountsForward() {
        assertThat(Timesheet.total(List.of(shift("22:00", "06:00")))).isEqualTo("8:00");
        assertThat(Timesheet.total(List.of(shift("20:00", "00:00")))).isEqualTo("4:00");
        assertThat(Timesheet.total(List.of(shift("09:00", "12:00"), shift("23:30", "01:15")))).isEqualTo("4:45");
    }

    @Test
    void aTotalOverADayKeepsEveryHour() {
        assertThat(Timesheet.total(List.of(
                shift("06:00", "13:00"), shift("06:00", "13:00"),
                shift("06:00", "13:00"), shift("06:00", "13:30")))).isEqualTo("28:30");
    }
}
