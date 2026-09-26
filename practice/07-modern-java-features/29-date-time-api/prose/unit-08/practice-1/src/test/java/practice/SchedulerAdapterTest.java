package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerAdapterTest {

    /** Like the old scheduler: it keeps the very Date objects it is given. */
    private static final class OldScheduler implements SchedulerAdapter.LegacyScheduler {
        private final Map<String, Date> runs = new HashMap<>();

        @Override
        public void schedule(String job, Date when) {
            runs.put(job, when);
        }

        @Override
        public Date nextRun(String job) {
            return runs.get(job);
        }
    }

    private TimeZone saved;

    /** A server whose default zone is far from UTC, so a conversion that leans on it shows. */
    @BeforeEach
    void aServerOnKiritimati() {
        saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
    }

    @AfterEach
    void restoreTheZone() {
        TimeZone.setDefault(saved);
    }

    private static ZonedDateTime utc(int h) {
        return ZonedDateTime.of(2024, 3, 15, h, 0, 0, 0, ZoneOffset.UTC);
    }

    @Test
    void schedulesAndReadsBackAJob() {
        SchedulerAdapter adapter = new SchedulerAdapter(new OldScheduler());
        adapter.schedule("backup", utc(10));
        assertThat(adapter.nextRun("backup", ZoneOffset.UTC)).contains(utc(10));
        adapter.schedule("tokyo", ZonedDateTime.of(2024, 3, 15, 19, 0, 0, 0, ZoneId.of("Asia/Tokyo")));
        assertThat(adapter.nextRun("tokyo", ZoneOffset.UTC)).contains(utc(10));
        ZonedDateTime quarterSecond = utc(10).plusNanos(250_000_000);
        adapter.schedule("precise", quarterSecond);
        assertThat(adapter.nextRun("precise", ZoneOffset.UTC)).contains(quarterSecond);
    }

    @Test
    void eachJobKeepsItsOwnTime() {
        SchedulerAdapter adapter = new SchedulerAdapter(new OldScheduler());
        adapter.schedule("a", utc(10));
        adapter.schedule("b", utc(11));
        assertThat(adapter.nextRun("a", ZoneOffset.UTC)).contains(utc(10));
        assertThat(adapter.nextRun("b", ZoneOffset.UTC)).contains(utc(11));
    }

    @Test
    void aJobWithNoRunIsEmpty() {
        SchedulerAdapter adapter = new SchedulerAdapter(new OldScheduler());
        assertThat(adapter.nextRun("unknown", ZoneOffset.UTC)).isEmpty();
    }

    @Test
    void theRunIsReadInTheCallersZone() {
        SchedulerAdapter adapter = new SchedulerAdapter(new OldScheduler());
        adapter.schedule("backup", utc(10));
        assertThat(adapter.nextRun("backup", ZoneId.of("Asia/Tokyo")))
                .contains(ZonedDateTime.of(2024, 3, 15, 19, 0, 0, 0, ZoneId.of("Asia/Tokyo")));
    }
}
