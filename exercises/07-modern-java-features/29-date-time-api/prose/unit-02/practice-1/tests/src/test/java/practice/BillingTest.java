package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BillingTest {

    /** The data centre's clock: a fixed moment, in the data centre's zone. */
    private static Billing billingAt(String instant, String dataCentreZone) {
        return new Billing(Clock.fixed(Instant.parse(instant), ZoneId.of(dataCentreZone)));
    }

    private static Instant at(String text) {
        return Instant.parse(text);
    }

    @Test
    void aCustomerOnUtcGetsInvoicesAndStatementsOnTheirDates() {
        Billing billing = billingAt("2041-02-14T12:00:00Z", "Europe/Berlin");
        ZoneId reykjavik = ZoneId.of("Atlantic/Reykjavik");

        assertThat(billing.invoiceDate(at("2026-07-08T21:40:00Z"), reykjavik))
                .isEqualTo(LocalDate.of(2026, 7, 8));
        assertThat(billing.statement(LocalDate.of(2026, 7, 8), reykjavik, List.of(
                at("2026-07-07T23:59:59Z"), at("2026-07-08T00:00:00Z"),
                at("2026-07-08T23:59:59Z"), at("2026-07-09T00:00:00Z"))))
                .containsExactly(at("2026-07-08T00:00:00Z"), at("2026-07-08T23:59:59Z"));
    }

    @Test
    void anEveningPaymentWestOfUtcIsDatedThatEvening() {
        Billing billing = billingAt("2041-02-14T12:00:00Z", "Europe/Berlin");

        assertThat(billing.invoiceDate(at("2026-07-08T04:15:00Z"), ZoneId.of("America/Los_Angeles")))
                .isEqualTo(LocalDate.of(2026, 7, 7));
    }

    @Test
    void aStatementEastOfUtcHoldsThePaymentsOfTheCustomersDay() {
        Billing billing = billingAt("2041-02-14T12:00:00Z", "Europe/Berlin");
        ZoneId tokyo = ZoneId.of("Asia/Tokyo");

        assertThat(billing.statement(LocalDate.of(2026, 7, 8), tokyo, List.of(
                at("2026-07-07T14:59:00Z"), at("2026-07-07T16:00:00Z"),
                at("2026-07-08T09:00:00Z"), at("2026-07-08T16:00:00Z"))))
                .containsExactly(at("2026-07-07T16:00:00Z"), at("2026-07-08T09:00:00Z"));
    }

    @Test
    void todayIsTheCustomersDateNotTheDataCentres() {
        Billing billing = billingAt("2041-02-14T23:30:00Z", "Europe/Berlin");

        assertThat(billing.today(ZoneId.of("America/Sao_Paulo"))).isEqualTo(LocalDate.of(2041, 2, 14));
        assertThat(billing.today(ZoneId.of("Pacific/Auckland"))).isEqualTo(LocalDate.of(2041, 2, 15));
    }

    @Test
    void todayFollowsTheServicesClock() {
        Billing billing = billingAt("2041-06-01T12:00:00Z", "UTC");

        assertThat(billing.today(ZoneId.of("Europe/Lisbon"))).isEqualTo(LocalDate.of(2041, 6, 1));
        assertThat(billing.todaysStatement(ZoneId.of("Europe/Lisbon"),
                List.of(at("2041-05-30T12:00:00Z"), at("2041-06-01T08:00:00Z"), at("2041-06-02T12:00:00Z"))))
                .containsExactly(at("2041-06-01T08:00:00Z"));
    }
}
