package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvitesTest {

    private static ZonedDateTime meeting(String local, String zone) {
        return ZonedDateTime.of(LocalDateTime.parse(local), ZoneId.of(zone));
    }

    // ZonedDateTime is compared as text: equal moments in different zones must not pass.

    @Test
    void zonesThatShareAClockShowTheSameTime() {
        ZonedDateTime m = meeting("2026-06-10T11:00", "Europe/Lisbon");
        assertThat(Invites.asSeenBy(m, ZoneId.of("Europe/London")).toString())
                .isEqualTo("2026-06-10T11:00+01:00[Europe/London]");
        assertThat(Invites.moveToOffice(m, ZoneId.of("Europe/London")).toString())
                .isEqualTo("2026-06-10T11:00+01:00[Europe/London]");
    }

    @Test
    void theInviteHasOneLinePerAttendeeInOrder() {
        ZonedDateTime m = meeting("2026-06-10T11:00", "Europe/Lisbon");
        assertThat(Invites.inviteLines(m, List.of(ZoneId.of("Atlantic/Madeira"), ZoneId.of("Europe/London"),
                ZoneId.of("Europe/Lisbon"))))
                .containsExactly("Atlantic/Madeira: 2026-06-10T11:00", "Europe/London: 2026-06-10T11:00",
                        "Europe/Lisbon: 2026-06-10T11:00");
    }

    @Test
    void anAttendeeElsewhereSeesTheMomentOnTheirOwnClock() {
        assertThat(Invites.asSeenBy(meeting("2026-11-02T08:30", "America/Chicago"), ZoneId.of("Asia/Tokyo")).toString())
                .isEqualTo("2026-11-02T23:30+09:00[Asia/Tokyo]");
        assertThat(Invites.asSeenBy(meeting("2026-08-14T18:00", "America/Los_Angeles"), ZoneId.of("Pacific/Auckland")).toString())
                .isEqualTo("2026-08-15T13:00+12:00[Pacific/Auckland]");
    }

    @Test
    void theDifferenceBetweenTwoZonesIsTheOneOnTheMeetingsDate() {
        assertThat(Invites.asSeenBy(meeting("2026-03-15T10:00", "America/Chicago"), ZoneId.of("Europe/Lisbon")).toString())
                .isEqualTo("2026-03-15T15:00Z[Europe/Lisbon]");
        assertThat(Invites.asSeenBy(meeting("2026-10-28T09:00", "Europe/Madrid"), ZoneId.of("America/Toronto")).toString())
                .isEqualTo("2026-10-28T04:00-04:00[America/Toronto]");
    }

    @Test
    void aMovedMeetingKeepsItsClockTime() {
        assertThat(Invites.moveToOffice(meeting("2026-05-05T09:00", "Europe/Berlin"), ZoneId.of("Asia/Singapore")).toString())
                .isEqualTo("2026-05-05T09:00+08:00[Asia/Singapore]");
        assertThat(Invites.moveToOffice(meeting("2026-12-01T16:30", "America/Denver"), ZoneId.of("Europe/Warsaw")).toString())
                .isEqualTo("2026-12-01T16:30+01:00[Europe/Warsaw]");
    }
}
