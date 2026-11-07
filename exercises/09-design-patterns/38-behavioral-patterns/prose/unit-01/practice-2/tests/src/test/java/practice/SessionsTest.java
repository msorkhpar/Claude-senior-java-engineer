package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import practice.Sessions.Session;

class SessionsTest {

    static final long HOUR = 3_600_000L;
    static final long DAY = 24 * HOUR;
    static final long START = 1_735_689_600_000L;   // 2025-01-01T00:00Z

    static List<String> ids(List<Session> sessions) {
        return sessions.stream().map(Session::id).toList();
    }

    @Test
    void sessionsOfTheLastHourListNewestFirst() {
        List<Session> seen = List.of(
                new Session("s-31", "u-1", START + 12 * 60_000),
                new Session("s-07", "u-2", START + 55 * 60_000),
                new Session("s-19", "u-3", START + 3 * 60_000),
                new Session("s-02", "u-4", START + 41 * 60_000));
        assertThat(ids(Sessions.forAdminPage(seen))).containsExactly("s-07", "s-02", "s-31", "s-19");
    }

    @Test
    void sessionsWeeksApartListNewestFirst() {
        List<Session> seen = List.of(
                new Session("s-40", "u-1", START),
                new Session("s-41", "u-2", START + 33 * DAY),
                new Session("s-42", "u-3", START + 12 * DAY),
                new Session("s-43", "u-4", START + 70 * DAY),
                new Session("s-44", "u-5", START + 48 * DAY));
        assertThat(ids(Sessions.forAdminPage(seen))).containsExactly("s-43", "s-44", "s-41", "s-42", "s-40");

        Random random = new Random(7);
        List<Session> season = new ArrayList<>();
        for (int i = 0; i < 3_000; i++) {
            season.add(new Session("s-" + (1_000 + i), "u-" + i, START + (long) (random.nextDouble() * 200 * DAY)));
        }
        List<Session> page = Sessions.forAdminPage(season);
        assertThat(page).isSortedAccordingTo(Comparator.comparingLong(Session::lastSeenMillis).reversed());
    }

    @Test
    void sessionsSeenAtTheSameMomentListByIdAToZ() {
        long t = START + 5 * HOUR;
        List<Session> seen = List.of(
                new Session("s-88", "u-1", t),
                new Session("s-12", "u-2", t),
                new Session("s-90", "u-3", t + 60_000),
                new Session("s-35", "u-4", t));
        assertThat(ids(Sessions.forAdminPage(seen))).containsExactly("s-90", "s-12", "s-35", "s-88");
    }
}
