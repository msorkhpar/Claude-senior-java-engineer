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
    private final Date reused = new Date(); // one Date for every call, to save garbage

    public SchedulerAdapter(LegacyScheduler legacy) {
        this.legacy = legacy;
    }

    /** Schedules {@code job} at the moment {@code when}. */
    public void schedule(String job, ZonedDateTime when) {
        reused.setTime(when.toInstant().toEpochMilli());
        legacy.schedule(job, reused);
    }

    /** The job's next run in {@code zone}, or empty when there is none. */
    public Optional<ZonedDateTime> nextRun(String job, ZoneId zone) {
        Date next = legacy.nextRun(job);
        if (next == null) {
            return Optional.empty();
        }
        return Optional.of(next.toInstant().atZone(zone));
    }
}
