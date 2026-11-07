package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SettlementReportTest {

    private TimeZone saved;

    @BeforeEach
    void serverInSaoPaulo() {
        saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
    }

    @AfterEach
    void restoreDefaultZone() {
        TimeZone.setDefault(saved);
    }

    private static long millis(String instant) {
        return Instant.parse(instant).toEpochMilli();
    }

    @Test
    void totalsPaymentsPerBusinessDay() {
        var totals = SettlementReport.totals(List.of(
                new SettlementReport.Payment(new Date(millis("2025-11-28T04:10:00Z")), 1_250),
                new SettlementReport.Payment(new Date(millis("2025-11-28T13:45:00Z")), 990),
                new SettlementReport.Payment(new Date(millis("2025-11-29T06:00:00Z")), 40_000)));

        assertThat(totals).containsExactly(
                entry(LocalDate.of(2025, 11, 28), 2_240L),
                entry(LocalDate.of(2025, 11, 29), 40_000L));
    }

    @Test
    void aPaymentLateInTheServersEveningCountsOnTheExchangesNextDay() {
        // 21:30 UTC is 18:30 on the 28th in Sao Paulo and 06:30 on the 29th in Tokyo
        Date at = new Date(millis("2025-11-28T21:30:00Z"));

        assertThat(SettlementReport.businessDay(at)).isEqualTo(LocalDate.of(2025, 11, 29));
    }

    @Test
    void aSettlementDayReadWithGetDateIsAccepted() {
        Date fromJdbc = java.sql.Date.valueOf("2025-11-27");      // as the driver builds it

        assertThat(SettlementReport.businessDay(fromJdbc)).isEqualTo(LocalDate.of(2025, 11, 27));
    }

    @Test
    void aSettlementDayReadWithGetDateKeepsItsDayOnAServerAheadOfTokyo() {
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Auckland"));
        Date fromJdbc = java.sql.Date.valueOf("2025-11-27");      // midnight in Auckland

        assertThat(SettlementReport.businessDay(fromJdbc)).isEqualTo(LocalDate.of(2025, 11, 27));
    }
}
