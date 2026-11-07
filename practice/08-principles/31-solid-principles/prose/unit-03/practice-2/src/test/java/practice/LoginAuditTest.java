package practice;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAuditTest {

    private static final Instant MORNING = Instant.parse("2025-11-04T07:41:12.018Z");
    private static final Instant NOON = Instant.parse("2025-11-04T12:03:55.940Z");
    private static final Instant EVENING = Instant.parse("2025-11-04T19:27:00.005Z");

    private static Date gateway(Instant at) {
        return new Date(at.toEpochMilli());
    }

    private static Date sessionStore(Instant at) {
        return new Timestamp(at.toEpochMilli());
    }

    @Test
    void listsTheGatewaysLoginsInOrderEachOnce() {
        List<Date> times = List.of(gateway(EVENING), gateway(MORNING), gateway(EVENING), gateway(NOON));

        assertThat(LoginAudit.distinctMoments(times)).containsExactly(MORNING, NOON, EVENING);
    }

    @Test
    void aLoginSeenFirstByTheGatewayThenByTheSessionStoreCountsOnce() {
        List<Date> times = List.of(gateway(NOON), gateway(MORNING), sessionStore(NOON), sessionStore(MORNING));

        assertThat(LoginAudit.distinctMoments(times)).containsExactly(MORNING, NOON);
    }

    @Test
    void aLoginSeenFirstByTheSessionStoreThenByTheGatewayCountsOnce() {
        List<Date> times = List.of(sessionStore(EVENING), sessionStore(NOON), gateway(EVENING), gateway(NOON));

        assertThat(LoginAudit.distinctMoments(times)).containsExactly(NOON, EVENING);
    }
}
