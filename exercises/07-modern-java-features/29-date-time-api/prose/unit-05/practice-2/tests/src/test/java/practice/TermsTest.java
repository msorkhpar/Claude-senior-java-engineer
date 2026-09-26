package practice;

import org.junit.jupiter.api.Test;

import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class TermsTest {

    @Test
    void namesEachPartInYearsMonthsAndDays() {
        assertThat(Terms.describe(Period.of(0, 14, 3))).isEqualTo("1 year, 2 months, 3 days");
        assertThat(Terms.describe(Period.of(2, 1, 1))).isEqualTo("2 years, 1 month, 1 day");
        assertThat(Terms.describe(Period.ofMonths(6))).isEqualTo("6 months");
        assertThat(Terms.describe(Period.of(0, 24, 0))).isEqualTo("2 years");
        assertThat(Terms.describe(Period.ofDays(10))).isEqualTo("10 days");
    }

    @Test
    void daysAreNeverFoldedIntoMonths() {
        assertThat(Terms.describe(Period.ofDays(45))).isEqualTo("45 days");
        assertThat(Terms.describe(Period.of(0, 11, 40))).isEqualTo("11 months, 40 days");
        assertThat(Terms.describe(Period.ofDays(400))).isEqualTo("400 days");
    }

    @Test
    void mixedSignsAreBalanced() {
        assertThat(Terms.describe(Period.of(2, -3, 0))).isEqualTo("1 year, 9 months");
        assertThat(Terms.describe(Period.of(1, -2, 5))).isEqualTo("10 months, 5 days");
    }

    @Test
    void anEmptyTermReadsZeroDays() {
        assertThat(Terms.describe(Period.ZERO)).isEqualTo("0 days");
        assertThat(Terms.describe(Period.of(1, -12, 0))).isEqualTo("0 days");
    }
}
