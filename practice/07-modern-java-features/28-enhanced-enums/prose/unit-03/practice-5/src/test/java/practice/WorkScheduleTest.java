package practice;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkScheduleTest {

    @Test
    void readsEachDaysHours() {
        assertThat(WorkSchedule.MONDAY.start()).contains(LocalTime.of(9, 0));
        assertThat(WorkSchedule.MONDAY.hours()).isEqualTo(Duration.ofHours(8));
        assertThat(WorkSchedule.FRIDAY.hours()).isEqualTo(Duration.ofHours(7));
        assertThat(WorkSchedule.SATURDAY.isWorkDay()).isTrue();
        assertThat(WorkSchedule.SATURDAY.hours()).isEqualTo(Duration.ofHours(4));
        assertThat(WorkSchedule.forDate(LocalDate.of(2026, 9, 25))).isEqualTo(WorkSchedule.FRIDAY);
        assertThat(WorkSchedule.forDate(LocalDate.of(2026, 9, 27))).isEqualTo(WorkSchedule.SUNDAY);
    }

    @Test
    void sundayHasNoHours() {
        assertThat(WorkSchedule.SUNDAY.start()).isEmpty();
        assertThat(WorkSchedule.SUNDAY.hours()).isEqualTo(Duration.ZERO);
        assertThat(WorkSchedule.SUNDAY.isWorkDay()).isFalse();
    }

    @Test
    void saturdayWorksButIsNotRequired() {
        assertThat(WorkSchedule.requiredHoursPerWeek()).isEqualTo(Duration.ofHours(39));
    }
}
