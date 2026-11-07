package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenewalsTest {

    private static Renewals.Subscription sub(String customer, String zone, String paidAt) {
        return new Renewals.Subscription(customer, ZoneId.of(zone), Instant.parse(paidAt));
    }

    @Test
    void renewsAnAprilPaymentOnTheSameDayOfMay() {
        Renewals.Subscription s = sub("Jane Doe", "America/Sao_Paulo", "2026-04-10T15:00:00Z");

        assertThat(Renewals.nextRenewal(s)).isEqualTo(Instant.parse("2026-05-10T15:00:00Z"));
        assertThat(Renewals.renewalDate(s)).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(Renewals.receiptLine(s)).isEqualTo("Jane Doe: renews on 2026-05-10");
        assertThat(Renewals.dueBy(List.of(s), Instant.parse("2026-05-10T14:59:59Z"))).isEmpty();
        assertThat(Renewals.dueBy(List.of(s), Instant.parse("2026-05-10T15:00:00Z"))).containsExactly("Jane Doe");
    }

    @Test
    void renewsAFebruaryPaymentOnTheSameDayOfMarch() {
        Renewals.Subscription s = sub("John Roe", "Africa/Nairobi", "2026-02-05T07:00:00Z");

        assertThat(Renewals.nextRenewal(s)).isEqualTo(Instant.parse("2026-03-05T07:00:00Z"));
    }

    @Test
    void countsTheMonthFromTheDayThePaymentWasMadeInTheCustomersZone() {
        // 2026-08-30T20:00Z is 31 August, 05:00 in Seoul
        Renewals.Subscription s = sub("Alex Poe", "Asia/Seoul", "2026-08-30T20:00:00Z");

        assertThat(Renewals.nextRenewal(s)).isEqualTo(Instant.parse("2026-09-29T20:00:00Z"));
    }

    @Test
    void printsTheRenewalDateFromTheCustomersCalendar() {
        // renews at 2026-07-20T16:30Z, which is 21 July, 01:30 in Seoul
        Renewals.Subscription s = sub("Alex Poe", "Asia/Seoul", "2026-06-20T16:30:00Z");

        assertThat(Renewals.renewalDate(s)).isEqualTo(LocalDate.of(2026, 7, 21));
        assertThat(Renewals.receiptLine(s)).isEqualTo("Alex Poe: renews on 2026-07-21");
    }
}
