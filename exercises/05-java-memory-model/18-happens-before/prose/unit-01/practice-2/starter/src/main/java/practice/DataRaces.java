package practice;

import java.util.List;

public final class DataRaces {

    private DataRaces() {
    }

    public enum Kind { READ, WRITE, VOLATILE_READ, VOLATILE_WRITE, LOCK, UNLOCK, START, JOIN }

    /** One action of a trace: the thread that did it, what it did, and what it did it to. */
    public record Action(String thread, Kind kind, String target) {
    }

    /** Returns every data race as a pair [i, j] with i < j, sorted. */
    public static List<int[]> find(List<Action> trace) {
        throw new UnsupportedOperationException("write find");
    }
}
