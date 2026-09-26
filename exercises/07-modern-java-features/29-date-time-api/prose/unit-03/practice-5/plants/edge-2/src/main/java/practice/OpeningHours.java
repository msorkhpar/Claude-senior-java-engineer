package practice;

import java.time.LocalTime;

public final class OpeningHours {

    private OpeningHours() {
    }

    public static boolean isOpen(LocalTime time, LocalTime open, LocalTime close) {
        return !time.isBefore(open) && !time.isAfter(close); // a window taken to lie within one day
    }
}
