package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetTest {

    private static LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }

    @Test
    void aMonthStartsOnItsFirstDayAndRunsInOrder() {
        List<LocalDate> rows = Timesheet.days(YearMonth.of(2027, 10));

        assertThat(rows).startsWith(d("2027-10-01"), d("2027-10-02"));
        assertThat(rows).isSorted().doesNotHaveDuplicates();
        assertThat(rows).allSatisfy(day -> assertThat(day).isBetween(d("2027-10-01"), d("2027-10-31")));
    }

    @Test
    void aPeriodsWeekdaysSkipTheWeekend() {
        assertThat(Timesheet.weekdays(d("2027-10-01"), d("2027-10-07")))   // Friday to Thursday
                .startsWith(d("2027-10-01"), d("2027-10-04"), d("2027-10-05"))
                .doesNotContain(d("2027-10-02"), d("2027-10-03"));
    }

    @Test
    void aMonthIncludesItsLastDay() {
        assertThat(Timesheet.days(YearMonth.of(2027, 9))).hasSize(30).endsWith(d("2027-09-30"));
        assertThat(Timesheet.days(YearMonth.of(2028, 2))).hasSize(29).endsWith(d("2028-02-29"));
    }

    @Test
    void aMonthHasNoDayOfTheNextMonth() {
        assertThat(Timesheet.days(YearMonth.of(2027, 2))).hasSize(28).endsWith(d("2027-02-28"));
        assertThat(Timesheet.days(YearMonth.of(2027, 11))).hasSize(30).endsWith(d("2027-11-30"));
    }

    @Test
    void aPeriodIncludesTheLastDayOnTheOrder() {
        assertThat(Timesheet.weekdays(d("2027-10-01"), d("2027-10-07")))
                .containsExactly(d("2027-10-01"), d("2027-10-04"), d("2027-10-05"), d("2027-10-06"),
                        d("2027-10-07"));
        assertThat(Timesheet.weekdays(d("2027-12-01"), d("2027-12-01"))).containsExactly(d("2027-12-01"));
    }
}
