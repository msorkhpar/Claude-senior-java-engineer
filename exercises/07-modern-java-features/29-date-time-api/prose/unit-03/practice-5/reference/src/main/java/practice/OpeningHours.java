package practice;

import java.time.LocalTime;

public final class OpeningHours {

    private OpeningHours() {
    }

    /** Whether time is within open..close, both included; a close before open runs overnight. */
    public static boolean isOpen(LocalTime time, LocalTime open, LocalTime close) {
        if (!open.isAfter(close)) {
            return !time.isBefore(open) && !time.isAfter(close);
        }
        // overnight: from open up to midnight, or from midnight up to close
        return !time.isBefore(open) || !time.isAfter(close);
    }
}
