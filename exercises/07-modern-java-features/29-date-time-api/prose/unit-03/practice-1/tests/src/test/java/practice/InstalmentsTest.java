package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InstalmentsTest {

    private static LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }

    @Test
    void aYearOfInstalmentsFallsOncePerMonthInOrder() {
        LocalDate first = d("2027-03-15");
        List<LocalDate> dates = Instalments.schedule(first, 12);

        assertThat(dates).hasSize(12);
        assertThat(dates.get(0)).isEqualTo(first);
        for (int n = 0; n < 12; n++) {
            assertThat(YearMonth.from(dates.get(n))).isEqualTo(YearMonth.from(first).plusMonths(n));
        }
    }

    @Test
    void aDayEveryMonthHasKeepsItsDayOfMonth() {
        assertThat(Instalments.schedule(d("2027-01-10"), 4))
                .containsExactly(d("2027-01-10"), d("2027-02-10"), d("2027-03-10"), d("2027-04-10"));
    }

    @Test
    void aShortMonthDoesNotMoveTheMonthsAfterIt() {
        assertThat(Instalments.schedule(d("2027-01-30"), 5))
                .containsExactly(d("2027-01-30"), d("2027-02-28"), d("2027-03-30"), d("2027-04-30"),
                        d("2027-05-30"));
    }

    @Test
    void aDayTheMonthLacksBecomesItsLastDay() {
        assertThat(Instalments.dueDate(d("2027-01-29"), 1)).isEqualTo(d("2027-02-28"));
        assertThat(Instalments.dueDate(d("2027-08-31"), 1)).isEqualTo(d("2027-09-30"));
    }

    @Test
    void aLoanStartingOnAShortMonthsLastDayStaysOnLastDays() {
        assertThat(Instalments.schedule(d("2027-06-30"), 4))
                .containsExactly(d("2027-06-30"), d("2027-07-31"), d("2027-08-31"), d("2027-09-30"));
        assertThat(Instalments.dueDate(d("2028-02-29"), 1)).isEqualTo(d("2028-03-31"));
    }

    @Test
    void aThirtiethThatEndsItsMonthIsAMonthEnd() {
        assertThat(Instalments.dueDate(d("2027-11-30"), 1)).isEqualTo(d("2027-12-31"));
        assertThat(Instalments.isMonthEnd(d("2027-04-30"))).isTrue();
    }

    @Test
    void februaryEndsOnTheTwentyEighthOnlyInACommonYear() {
        assertThat(Instalments.dueDate(d("2027-02-28"), 1)).isEqualTo(d("2027-03-31"));
        assertThat(Instalments.dueDate(d("2028-02-28"), 1)).isEqualTo(d("2028-03-28"));
        assertThat(Instalments.isMonthEnd(LocalDate.of(2027, Month.FEBRUARY, 28))).isTrue();
        assertThat(Instalments.isMonthEnd(LocalDate.of(2028, Month.FEBRUARY, 28))).isFalse();
    }
}
