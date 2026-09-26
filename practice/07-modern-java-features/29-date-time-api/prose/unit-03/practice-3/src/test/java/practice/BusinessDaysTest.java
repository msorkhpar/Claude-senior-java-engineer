package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class BusinessDaysTest {

    private static final LocalDate MONDAY = LocalDate.of(2024, 3, 11);

    private static List<LocalDate> holidays(String... dates) {
        List<LocalDate> list = new ArrayList<>();
        for (String date : dates) {
            list.add(LocalDate.parse(date));
        }
        return list;
    }

    @Test
    void countsAndAddsWeekdays() {
        assertThat(BusinessDays.count(MONDAY, MONDAY.plusWeeks(1), List.of())).isEqualTo(5);
        assertThat(BusinessDays.count(MONDAY, MONDAY.plusWeeks(2), List.of())).isEqualTo(10);
        assertThat(BusinessDays.add(LocalDateTime.of(2024, 3, 11, 9, 30), 2)).isEqualTo(LocalDateTime.of(2024, 3, 13, 9, 30));
        assertThat(BusinessDays.add(LocalDateTime.of(2024, 3, 12, 9, 30), 3)).isEqualTo(LocalDateTime.of(2024, 3, 15, 9, 30));
    }

    @Test
    void aWeekendOnlyRangeHasNone() {
        assertThat(BusinessDays.count(LocalDate.of(2024, 3, 16), LocalDate.of(2024, 3, 18), List.of())).isEqualTo(0);
        assertThat(BusinessDays.count(LocalDate.of(2024, 3, 16), LocalDate.of(2024, 3, 19), List.of())).isEqualTo(1);
    }

    @Test
    void holidaysMatchByValue() {
        assertThat(BusinessDays.count(MONDAY, MONDAY.plusWeeks(1), holidays("2024-03-13"))).isEqualTo(4);
        assertThat(BusinessDays.count(MONDAY, MONDAY.plusWeeks(2), holidays("2024-03-13", "2024-03-22"))).isEqualTo(8);
    }

    @Test
    void aWeekendHolidayIsNotTakenOffTwice() {
        assertThat(BusinessDays.count(MONDAY, MONDAY.plusWeeks(1), holidays("2024-03-16"))).isEqualTo(5);
    }

    @Test
    void addingSkipsEveryWeekendDay() {
        assertThat(BusinessDays.add(LocalDateTime.of(2024, 3, 15, 17, 45), 1)).isEqualTo(LocalDateTime.of(2024, 3, 18, 17, 45));
        assertThat(BusinessDays.add(LocalDateTime.of(2024, 3, 14, 8, 0), 3)).isEqualTo(LocalDateTime.of(2024, 3, 19, 8, 0));
        assertThat(BusinessDays.add(LocalDateTime.of(2024, 3, 16, 10, 0), 5)).as("from a Saturday").isEqualTo(LocalDateTime.of(2024, 3, 22, 10, 0));
    }
}
