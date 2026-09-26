package practice;

import java.time.LocalTime;

public final class OpeningHours {

    private OpeningHours() {
    }

    /** Whether time is within open..close, both included; a close before open runs overnight. */
    public static boolean isOpen(LocalTime time, LocalTime open, LocalTime close) {
        throw new UnsupportedOperationException("write isOpen");
    }
}
