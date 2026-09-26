package practice;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogTest {

    /** A clock the test moves by hand. */
    private static final class HandClock extends Clock {
        private Instant now;

        HandClock(Instant start) {
            this.now = start;
        }

        void advance(Duration by) {
            now = now.plus(by);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static final Instant NOON = Instant.parse("2024-03-15T12:00:00Z");

    @Test
    void recordsAtTheClocksInstant() {
        HandClock clock = new HandClock(NOON);
        AuditLog log = new AuditLog(clock);
        AuditLog.Entry login = log.record("login");
        assertThat(login.at()).isEqualTo(NOON);
        assertThat(login.action()).isEqualTo("login");
        clock.advance(Duration.ofMinutes(5));
        assertThat(log.record("view").at()).isEqualTo(Instant.parse("2024-03-15T12:05:00Z"));
        assertThat(log.actionsWithin(Duration.ofMinutes(10))).containsExactly("login", "view");
        assertThat(log.actionsWithin(Duration.ofMinutes(2))).containsExactly("view");

        HandClock fine = new HandClock(NOON);
        AuditLog precise = new AuditLog(fine);
        precise.record("old");
        fine.advance(Duration.ofMinutes(10).plusNanos(500_000));
        precise.record("new");
        assertThat(precise.actionsWithin(Duration.ofMinutes(10))).as("half a millisecond too old").containsExactly("new");

        HandClock quick = new HandClock(NOON);
        AuditLog brief = new AuditLog(quick);
        brief.record("first");
        quick.advance(Duration.ofMillis(1200));
        brief.record("second");
        assertThat(brief.actionsWithin(Duration.ofMillis(1500))).as("a window of 1.5 s").containsExactly("first", "second");
    }

    @Test
    void theWindowStartIsInside() {
        HandClock clock = new HandClock(NOON);
        AuditLog log = new AuditLog(clock);
        log.record("login");
        clock.advance(Duration.ofMinutes(10));
        assertThat(log.actionsWithin(Duration.ofMinutes(10))).containsExactly("login");
    }

    @Test
    void theWindowEndsAtTheClocksNow() {
        HandClock clock = new HandClock(NOON);
        AuditLog log = new AuditLog(clock);
        log.record("login");
        clock.advance(Duration.ofMinutes(1));
        log.record("view");
        clock.advance(Duration.ofHours(1));
        assertThat(log.actionsWithin(Duration.ofMinutes(10))).isEmpty();
    }
}
