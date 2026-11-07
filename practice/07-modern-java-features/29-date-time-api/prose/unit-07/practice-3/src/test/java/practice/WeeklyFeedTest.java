package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class WeeklyFeedTest {

    private static ZonedDateTime first(String local, String zone) {
        return ZonedDateTime.of(LocalDateTime.parse(local), ZoneId.of(zone));
    }

    @Test
    void weeksWithoutAClockChangeKeepTheirTimeAndOffset() {
        assertThat(WeeklyFeed.feed(first("2026-06-03T18:00", "Australia/Sydney"), 3))
                .containsExactly("2026-06-03T18:00+10:00", "2026-06-10T18:00+10:00", "2026-06-17T18:00+10:00");
    }

    @Test
    void aSingleSessionIsTheFirstOne() {
        assertThat(WeeklyFeed.feed(first("2026-01-12T07:15", "America/Chicago"), 1))
                .containsExactly("2026-01-12T07:15-06:00");
    }

    @Test
    void afterTheClocksChangeTheClassKeepsItsTimeOnTheGymsClock() {
        assertThat(WeeklyFeed.feed(first("2026-09-30T18:00", "Australia/Sydney"), 2))
                .containsExactly("2026-09-30T18:00+10:00", "2026-10-07T18:00+11:00");
        assertThat(WeeklyFeed.feed(first("2026-10-28T19:30", "America/Chicago"), 2))
                .containsExactly("2026-10-28T19:30-05:00", "2026-11-04T19:30-06:00");
    }

    @Test
    void aSessionThatFallsInAGapMovesOnlyThatWeek() {
        assertThat(WeeklyFeed.feed(first("2026-03-01T02:30", "America/Chicago"), 3))
                .containsExactly("2026-03-01T02:30-06:00", "2026-03-08T03:30-05:00", "2026-03-15T02:30-05:00");
    }
}
