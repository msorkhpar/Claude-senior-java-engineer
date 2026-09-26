package practice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

public final class SchedulerAdapter {

    /** The old scheduler's API. */
    public interface LegacyScheduler {
        void schedule(String job, Date when);

        Date nextRun(String job);
    }

    private final LegacyScheduler legacy;

    public SchedulerAdapter(LegacyScheduler legacy) {
        this.legacy = legacy;
    }

    /** Schedules {@code job} at the moment {@code when}. */
    public void schedule(String job, ZonedDateTime when) {
        legacy.schedule(job, Date.from(when.toInstant()));
    }

    /** The job's next run in {@code zone}, or empty when there is none. */
    public Optional<ZonedDateTime> nextRun(String job, ZoneId zone) {
        return Optional.of(legacy.nextRun(job).toInstant().atZone(zone));
    }
}
