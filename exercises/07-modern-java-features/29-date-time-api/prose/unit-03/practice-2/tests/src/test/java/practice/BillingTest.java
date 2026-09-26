package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BillingTest {

    @Test
    void billsOnTheSameDayEachMonth() {
        LocalDate first = LocalDate.parse("2024-01-15");
        assertThat(Billing.nthBill(first, 0)).isEqualTo(LocalDate.of(2024, 1, 15));
        assertThat(Billing.nthBill(first, 1)).isEqualTo(LocalDate.of(2024, 2, 15));
        assertThat(Billing.nthBill(first, 13)).isEqualTo(LocalDate.of(2025, 2, 15));
        assertThat(Billing.schedule(first, 3))
                .containsExactly(LocalDate.of(2024, 1, 15), LocalDate.of(2024, 2, 15), LocalDate.of(2024, 3, 15));
    }

    @Test
    void aShortMonthClampsToItsLastDay() {
        assertThat(Billing.nthBill(LocalDate.of(2024, 1, 31), 1)).isEqualTo(LocalDate.of(2024, 2, 29));
        assertThat(Billing.nthBill(LocalDate.of(2023, 1, 31), 1)).isEqualTo(LocalDate.of(2023, 2, 28));
        assertThat(Billing.nthBill(LocalDate.of(2024, 3, 31), 1)).isEqualTo(LocalDate.of(2024, 4, 30));
    }

    @Test
    void eachBillCountsFromTheFirst() {
        LocalDate first = LocalDate.of(2024, 1, 31);
        assertThat(Billing.nthBill(first, 2)).isEqualTo(LocalDate.of(2024, 3, 31));
        assertThat(Billing.schedule(first, 4)).containsExactly(
                LocalDate.of(2024, 1, 31), LocalDate.of(2024, 2, 29), LocalDate.of(2024, 3, 31), LocalDate.of(2024, 4, 30));
    }
}
