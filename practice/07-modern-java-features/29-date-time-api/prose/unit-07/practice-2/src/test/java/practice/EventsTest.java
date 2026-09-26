package practice;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventsTest {

    /** Parses a fresh object each time, so no two events share an instance. */
    private static ZonedDateTime at(String text) {
        return ZonedDateTime.parse(new StringBuilder(text).toString());
    }

    @Test
    void dropsARepeatedEvent() {
        ZonedDateTime noon = at("2024-03-15T12:00-04:00[America/New_York]");
        ZonedDateTime one = at("2024-03-15T13:00-04:00[America/New_York]");
        ZonedDateTime noonAgain = at("2024-03-15T12:00-04:00[America/New_York]");
        assertThat(Events.distinctMoments(List.of(noon, one, noonAgain))).containsExactly(noon, one);
        assertThat(Events.distinctMoments(List.of())).isEmpty();
        ZonedDateTime halfASecondLater = at("2024-03-15T12:00:00.5-04:00[America/New_York]");
        assertThat(Events.distinctMoments(List.of(noon, halfASecondLater))).containsExactly(noon, halfASecondLater);
    }

    @Test
    void theSameMomentInAnotherZoneIsADuplicate() {
        ZonedDateTime newYork = at("2024-03-15T12:00-04:00[America/New_York]");
        List<ZonedDateTime> events = List.of(newYork,
                at("2024-03-15T16:00Z[Europe/London]"),
                at("2024-03-15T16:00Z[UTC]"),
                at("2024-03-15T12:00-04:00"));
        assertThat(Events.distinctMoments(events)).containsExactly(newYork);
    }

    @Test
    void theSameWallClockInAnotherZoneIsNot() {
        List<ZonedDateTime> events = List.of(
                at("2024-03-15T12:00-04:00[America/New_York]"),
                at("2024-03-15T12:00Z[Europe/London]"),
                at("2024-03-15T12:00+09:00[Asia/Tokyo]"));
        assertThat(Events.distinctMoments(events)).containsExactlyElementsOf(events);
    }

    @Test
    void keepsTheOrderTheyCameIn() {
        ZonedDateTime one = at("2024-03-15T13:00-04:00[America/New_York]");
        ZonedDateTime london = at("2024-03-15T16:00Z[Europe/London]");
        ZonedDateTime noon = at("2024-03-15T12:00-04:00[America/New_York]");
        assertThat(Events.distinctMoments(List.of(one, london, noon))).containsExactly(one, london);
    }
}
