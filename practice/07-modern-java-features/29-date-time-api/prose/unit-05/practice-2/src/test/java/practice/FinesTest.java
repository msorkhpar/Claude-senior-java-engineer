package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FinesTest {

    private static final LocalDate ADULT = LocalDate.of(1983, 11, 2);

    @Test
    void chargesEachDayLateAtTheMembersRate() {
        assertThat(Fines.fineCents(ADULT, LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 9))).isEqualTo(125);
        assertThat(Fines.fineCents(ADULT, LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 4))).isZero();
        assertThat(Fines.fineCents(LocalDate.of(2012, 9, 14), LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 11))).isEqualTo(70);
    }

    @Test
    void anItemReturnedInALaterMonthIsChargedForEveryDay() {
        assertThat(Fines.daysOverdue(LocalDate.of(2026, 6, 28), LocalDate.of(2026, 8, 2))).isEqualTo(35);
        assertThat(Fines.fineCents(ADULT, LocalDate.of(2025, 12, 15), LocalDate.of(2026, 2, 15))).isEqualTo(62 * 25);
    }

    @Test
    void aMemberIsAYouthUntilTheEighteenthBirthday() {
        LocalDate born = LocalDate.of(2008, 8, 12);

        assertThat(Fines.ageOn(born, LocalDate.of(2026, 8, 9))).isEqualTo(17);
        assertThat(Fines.ageOn(born, LocalDate.of(2026, 8, 12))).isEqualTo(18);
        assertThat(Fines.fineCents(born, LocalDate.of(2026, 8, 6), LocalDate.of(2026, 8, 9))).isEqualTo(30);
    }

    @Test
    void anItemReturnedEarlyCostsNothing() {
        assertThat(Fines.fineCents(ADULT, LocalDate.of(2026, 5, 4), LocalDate.of(2026, 4, 30))).isZero();
    }
}
