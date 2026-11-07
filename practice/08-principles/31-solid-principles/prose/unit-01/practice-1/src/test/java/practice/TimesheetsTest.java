package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import practice.Timesheets.FloorReport;
import practice.Timesheets.Payroll;
import practice.Timesheets.Shift;

import static org.assertj.core.api.Assertions.assertThat;

class TimesheetsTest {

    private final Payroll payroll = new Payroll(2_400);      // 24.00 an hour, 40 cents a minute
    private final FloorReport report = new FloorReport();

    @Test
    void aWeekOfShiftsInWholeQuarterHoursIsPaidAndReportedAsWorked() {
        List<Shift> week = List.of(new Shift(510, 30), new Shift(555, 45));   // 8h00 and 8h30 worked

        assertThat(payroll.pay(week)).isEqualTo(990 * 40L);
        assertThat(report.line("Jane Doe", week)).isEqualTo("Jane Doe  16h30");
    }

    @Test
    void aShiftIsPaidInWholeQuarterHoursOfTimeWorked() {
        List<Shift> week = List.of(new Shift(530, 30), new Shift(436, 20));  // 8h20 and 6h56 worked

        assertThat(payroll.pay(week)).isEqualTo((495 + 405) * 40L);
    }

    @Test
    void aShortBreakCountsAsTimeOnTheFloor() {
        List<Shift> week = List.of(new Shift(495, 15), new Shift(480, 19));

        assertThat(report.line("John Roe", week)).isEqualTo("John Roe  16h15");
    }

    @Test
    void theFloorReportKeepsTheMinutesThatPayrollDoesNotPay() {
        List<Shift> week = List.of(new Shift(512, 30), new Shift(455, 40));  // 8h02 and 6h55 worked

        assertThat(report.line("John Roe", week)).isEqualTo("John Roe  14h57");
    }

    @Test
    void aShortBreakOnTheFloorIsStillNotPaid() {
        List<Shift> week = List.of(new Shift(495, 15), new Shift(570, 10));  // 8h00 and 9h20 worked

        assertThat(payroll.pay(week)).isEqualTo((480 + 555) * 40L);
    }
}
