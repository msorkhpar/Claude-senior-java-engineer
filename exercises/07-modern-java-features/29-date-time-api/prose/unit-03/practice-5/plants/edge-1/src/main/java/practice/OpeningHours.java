package practice;

import java.time.LocalTime;

public final class OpeningHours {

    private OpeningHours() {
    }

    public static boolean isOpen(LocalTime time, LocalTime open, LocalTime close) {
        if (!open.isAfter(close)) {
            return time.isAfter(open) && time.isBefore(close); // both ends left out
        }
        return time.isAfter(open) || time.isBefore(close);
    }
}
