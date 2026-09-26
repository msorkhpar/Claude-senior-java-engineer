package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorldClockTest {

    private static final Instant MOMENT = Instant.parse("2024-03-15T12:30:00Z");
    private static final List<ZoneId> OFFICES = List.of(
            ZoneId.of("America/New_York"), ZoneId.of("Europe/London"), ZoneId.of("Asia/Tokyo"));

    private Locale saved;

    @BeforeEach
    void aServerInTheUs() {
        saved = Locale.getDefault();
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void restoreTheLocale() {
        Locale.setDefault(saved);
    }

    /** A clock that moves one minute forward every time it is read. */
    private static final class MovingClock extends Clock {
        private int reads;

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return MOMENT.plusSeconds(60L * reads++);
        }
    }

    @Test
    void showsEachZonesLocalTime() {
        WorldClock clock = new WorldClock(Clock.fixed(MOMENT, ZoneOffset.UTC));
        assertThat(clock.show(OFFICES)).containsExactly(
                "America/New_York Fri 08:30 EDT", "Europe/London Fri 12:30 GMT", "Asia/Tokyo Fri 21:30 JST");
        assertThat(clock.show(List.of(ZoneId.of("Asia/Tokyo")))).containsExactly("Asia/Tokyo Fri 21:30 JST");
    }

    @Test
    void everyZoneShowsTheSameMoment() {
        WorldClock clock = new WorldClock(new MovingClock());
        assertThat(clock.show(OFFICES)).containsExactly(
                "America/New_York Fri 08:30 EDT", "Europe/London Fri 12:30 GMT", "Asia/Tokyo Fri 21:30 JST");
    }

    @Test
    void eachCallReadsTheClockAgain() {
        WorldClock clock = new WorldClock(new MovingClock());
        clock.show(OFFICES);
        assertThat(clock.show(List.of(ZoneId.of("Europe/London")))).containsExactly("Europe/London Fri 12:31 GMT");
    }

    /** Runs first, so a formatter cached from the server's locale is built while that locale is German. */
    @Test
    @Order(1)
    void englishOnAnyServer() {
        Locale.setDefault(Locale.GERMANY);
        WorldClock clock = new WorldClock(Clock.fixed(MOMENT, ZoneOffset.UTC));
        assertThat(clock.show(List.of(ZoneId.of("America/New_York")))).containsExactly("America/New_York Fri 08:30 EDT");
    }
}
