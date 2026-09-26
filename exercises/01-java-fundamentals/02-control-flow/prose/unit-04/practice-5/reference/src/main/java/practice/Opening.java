package practice;

import java.time.DayOfWeek;

public final class Opening {

    private Opening() {
    }

    /** Returns the opening hours of one day. */
    public static int hours(DayOfWeek day) {
        int hours;
        switch (day) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                hours = 9;
                break;
            case SATURDAY:
                hours = 5;
                break;
            case SUNDAY:
                hours = 0;
                break;
            default:
                throw new IllegalStateException("no such day: " + day);
        }
        return hours;
    }
}
