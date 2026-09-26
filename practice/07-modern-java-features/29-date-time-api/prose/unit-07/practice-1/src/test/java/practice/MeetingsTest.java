package practice;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MeetingsTest {

    private static final ZoneId NY = ZoneId.of("America/New_York");
    private static final ZoneId TORONTO = ZoneId.of("America/Toronto");
    private static final ZoneId LONDON = ZoneId.of("Europe/London");
    private static final ZoneId TOKYO = ZoneId.of("Asia/Tokyo");

    private static ZonedDateTime at(int y, int m, int d, int h, ZoneId zone) {
        return ZonedDateTime.of(y, m, d, h, 0, 0, 0, zone);
    }

    /** AssertJ compares ZonedDateTimes by instant; the text also pins the wall clock, offset and zone. */
    private static void same(ZonedDateTime actual, ZonedDateTime expected) {
        assertThat(actual.toString()).isEqualTo(expected.toString());
    }

    @Test
    void aZoneWithTheSameRulesChangesNothing() {
        ZonedDateTime noon = at(2024, 3, 15, 12, NY);
        same(Meetings.forAttendee(noon, TORONTO), at(2024, 3, 15, 12, TORONTO));
        same(Meetings.reschedule(noon, TORONTO), at(2024, 3, 15, 12, TORONTO));
        same(Meetings.forAttendee(noon, NY), noon);
    }

    @Test
    void anAttendeeSeesTheSameMoment() {
        ZonedDateTime noon = at(2024, 3, 15, 12, NY);
        same(Meetings.forAttendee(noon, LONDON), at(2024, 3, 15, 16, LONDON));
        assertThat(Meetings.forAttendee(noon, LONDON).toInstant()).isEqualTo(noon.toInstant());
        same(Meetings.forAttendee(at(2024, 3, 15, 20, NY), TOKYO), at(2024, 3, 16, 9, TOKYO));
    }

    @Test
    void aRescheduledMeetingKeepsItsWallClock() {
        ZonedDateTime noon = at(2024, 3, 15, 12, NY);
        same(Meetings.reschedule(noon, LONDON), at(2024, 3, 15, 12, LONDON));
        same(Meetings.reschedule(at(2024, 3, 15, 20, NY), TOKYO), at(2024, 3, 15, 20, TOKYO));
    }
}
