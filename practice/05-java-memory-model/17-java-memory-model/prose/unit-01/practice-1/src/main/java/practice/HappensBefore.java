package practice;

import java.util.List;

public final class HappensBefore {

    private HappensBefore() {
    }

    public enum Kind { WRITE, READ, VOLATILE_WRITE, VOLATILE_READ, LOCK, UNLOCK, START, JOIN }

    /** One action: the thread that performs it, what it does, and what it does it to. */
    public record Action(String thread, Kind kind, String target) {
    }

    /** Whether {@code trace.get(x)} happens-before {@code trace.get(y)} in this execution. */
    public static boolean happensBefore(List<Action> trace, int x, int y) {
        throw new UnsupportedOperationException("write happensBefore");
    }
}
