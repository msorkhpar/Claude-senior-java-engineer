package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionTest {

    private static LocalDate d(int y, int m, int day) {
        return LocalDate.of(y, m, day);
    }

    @Test
    void renewsOnTheSameDayOfMonth() {
        assertThat(Subscription.renewals(d(2024, 1, 15), Period.ofMonths(1), 3))
                .containsExactly(d(2024, 2, 15), d(2024, 3, 15), d(2024, 4, 15));
        assertThat(Subscription.renewals(d(2024, 1, 15), Period.ofMonths(3), 2))
                .containsExactly(d(2024, 4, 15), d(2024, 7, 15));
        assertThat(Subscription.renewals(d(2023, 6, 10), Period.ofYears(1), 2))
                .containsExactly(d(2024, 6, 10), d(2025, 6, 10));
        assertThat(Subscription.renewals(d(2024, 1, 15), Period.of(0, 1, 15), 1)).containsExactly(d(2024, 3, 1));
        assertThat(Subscription.renewals(d(2024, 1, 15), Period.ofMonths(1), 0)).isEmpty();
    }

    @Test
    void aMonthEndStartClampsToTheShorterMonth() {
        assertThat(Subscription.renewals(d(2024, 1, 31), Period.ofMonths(1), 1)).containsExactly(d(2024, 2, 29));
        assertThat(Subscription.renewals(d(2023, 1, 31), Period.ofMonths(1), 1)).containsExactly(d(2023, 2, 28));
        assertThat(Subscription.renewals(d(2024, 3, 31), Period.ofMonths(1), 1)).containsExactly(d(2024, 4, 30));
    }

    @Test
    void renewalsDoNotDriftAfterAShortMonth() {
        assertThat(Subscription.renewals(d(2024, 1, 31), Period.ofMonths(1), 4))
                .containsExactly(d(2024, 2, 29), d(2024, 3, 31), d(2024, 4, 30), d(2024, 5, 31));
        assertThat(Subscription.renewals(d(2024, 2, 29), Period.ofYears(1), 4))
                .containsExactly(d(2025, 2, 28), d(2026, 2, 28), d(2027, 2, 28), d(2028, 2, 29));
        assertThat(Subscription.renewals(d(2024, 1, 15), Period.of(0, 1, 15), 2))
                .containsExactly(d(2024, 3, 1), d(2024, 4, 14));
    }
}
