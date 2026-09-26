package practice;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

public enum WorkSchedule {
    MONDAY(DayOfWeek.MONDAY, "09:00", "17:00", true),
    TUESDAY(DayOfWeek.TUESDAY, "09:00", "17:00", true),
    WEDNESDAY(DayOfWeek.WEDNESDAY, "09:00", "17:00", true),
    THURSDAY(DayOfWeek.THURSDAY, "09:00", "17:00", true),
    FRIDAY(DayOfWeek.FRIDAY, "09:00", "16:00", true),
    SATURDAY(DayOfWeek.SATURDAY, "10:00", "14:00", false),
    SUNDAY(DayOfWeek.SUNDAY, null, null, false);

    private final DayOfWeek dayOfWeek;
    private final String startTime;
    private final String endTime;
    private final boolean required;

    WorkSchedule(DayOfWeek dayOfWeek, String startTime, String endTime, boolean required) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.required = required;
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    public boolean isRequired() {
        return required;
    }

    /** The start time, or empty when the day has none. */
    public Optional<LocalTime> start() {
        return Optional.ofNullable(startTime).map(LocalTime::parse);
    }

    /** The time from start to end, or zero when the day has no times. */
    public Duration hours() {
        if (startTime == null || endTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(LocalTime.parse(startTime), LocalTime.parse(endTime));
    }

    /** Whether the day has working times. */
    public boolean isWorkDay() {
        return startTime != null && endTime != null;
    }

    /** The constant for {@code date}'s day of the week. */
    public static WorkSchedule forDate(LocalDate date) {
        return Arrays.stream(values())
                .filter(s -> s.dayOfWeek == date.getDayOfWeek())
                .findFirst()
                .orElseThrow();
    }

    /** The total hours of the required days. */
    public static Duration requiredHoursPerWeek() {
        return Arrays.stream(values())
                .filter(WorkSchedule::isRequired)
                .map(WorkSchedule::hours)
                .reduce(Duration.ZERO, Duration::plus);
    }
}
