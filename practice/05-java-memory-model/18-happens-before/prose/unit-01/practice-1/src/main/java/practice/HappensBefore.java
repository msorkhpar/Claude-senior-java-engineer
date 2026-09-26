package practice;

import java.util.List;

public final class HappensBefore {

    private HappensBefore() {
    }

    public enum Kind { READ, WRITE, VOLATILE_READ, VOLATILE_WRITE, LOCK, UNLOCK, START, JOIN }

    /** One action of a trace: the thread that did it, what it did, and what it did it to. */
    public record Action(String thread, Kind kind, String target) {
    }

    /** Returns whether trace[a] happens-before trace[b] by the six rules. */
    public static boolean happensBefore(List<Action> trace, int a, int b) {
        throw new UnsupportedOperationException("write happensBefore");
    }
}
