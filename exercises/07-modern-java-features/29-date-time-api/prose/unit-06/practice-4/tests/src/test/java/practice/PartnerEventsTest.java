package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

/** The server's default zone is set to UTC for every test and restored afterwards. */
class PartnerEventsTest {

    private TimeZone before;

    @BeforeEach
    void serverOnUtc() {
        before = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void restoreTheDefault() {
        TimeZone.setDefault(before);
    }

    @Test
    void readsAnEventTimeWithAZeroOffset() {
        assertThat(PartnerEvents.receivedAt("2031-11-14T09:30:00+00:00"))
                .isEqualTo(Instant.parse("2031-11-14T09:30:00Z"));
        assertThat(PartnerEvents.receivedAt("2032-02-29T23:59:59+00:00"))
                .isEqualTo(Instant.parse("2032-02-29T23:59:59Z"));
    }

    @Test
    void showsAnAgentOnUtcTheEventOnTheirClock() {
        assertThat(PartnerEvents.historyLine("2031-11-14T09:30:00+00:00", "scanned at depot", ZoneId.of("UTC")))
                .isEqualTo("2031-11-14 09:30  scanned at depot");
    }

    @Test
    void anEventTimeAheadOfOrBehindUtcNamesItsOwnMoment() {
        assertThat(PartnerEvents.receivedAt("2031-11-14T15:00:00+05:30"))
                .isEqualTo(Instant.parse("2031-11-14T09:30:00Z"));
        assertThat(PartnerEvents.receivedAt("2031-11-14T06:30:00-03:00"))
                .isEqualTo(Instant.parse("2031-11-14T09:30:00Z"));
    }

    @Test
    void readsAnEventTimeWrittenWithZ() {
        assertThat(PartnerEvents.receivedAt("2031-11-14T09:30:00Z"))
                .isEqualTo(Instant.parse("2031-11-14T09:30:00Z"));
    }

    @Test
    void showsTheEventOnTheAgentsClockNotTheServers() {
        assertThat(PartnerEvents.forAgent(Instant.parse("2031-11-14T09:30:00Z"), ZoneId.of("Asia/Kolkata")))
                .isEqualTo("2031-11-14 15:00");
        assertThat(PartnerEvents.forAgent(Instant.parse("2031-11-14T01:10:00Z"), ZoneId.of("America/Sao_Paulo")))
                .isEqualTo("2031-11-13 22:10");
    }
}
