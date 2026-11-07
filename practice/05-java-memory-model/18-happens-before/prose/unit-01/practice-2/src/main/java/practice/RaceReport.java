package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * Finds the data races in one recorded run of a concurrency test. The replay harness records every
 * action of every thread in the order the actions ran.
 */
public class RaceReport {

    public enum Kind { READ, WRITE, VOLATILE_READ, VOLATILE_WRITE, LOCK, UNLOCK, START, JOIN }

    /**
     * One recorded action. {@code target} is a field for the four access kinds, a monitor for LOCK
     * and UNLOCK, and the other thread's name for START and JOIN. A JOIN is recorded only when the
     * join returned because that thread had ended.
     */
    public record Action(String thread, Kind kind, String target) {
    }

    private final List<Action> trace;

    public RaceReport(List<Action> trace) {
        this.trace = List.copyOf(trace);
    }

    /**
     * Every pair "i-j" (i < j) of plain accesses to the same field from two different threads, at
     * least one of them a write, that the memory model leaves unordered. Sorted by i, then j.
     */
    public List<String> races() {
        List<String> found = new ArrayList<>();
        for (int i = 0; i < trace.size(); i++) {
            for (int j = i + 1; j < trace.size(); j++) {
                Action a = trace.get(i), b = trace.get(j);
                boolean conflicting = isPlain(a) && isPlain(b)
                        && a.target().equals(b.target())
                        && !a.thread().equals(b.thread())
                        && (a.kind() == Kind.WRITE || b.kind() == Kind.WRITE);
                if (conflicting && !happensBefore(i, j)) found.add(i + "-" + j);
            }
        }
        return found;
    }

    private static boolean isPlain(Action a) {
        return a.kind() == Kind.READ || a.kind() == Kind.WRITE;
    }

    /** Whether the memory model orders action i before action j (i < j) directly, with no action between. */
    boolean edge(int i, int j) {
        // TODO: Decide whether one recorded action is ordered before a later one by a single
        //       rule of the memory model, without going through any other action.
        throw new IllegalStateException("TODO: edge");
    }

    /** Whether action i happens-before action j (i < j). */
    boolean happensBefore(int i, int j) {
        // TODO: Decide whether the memory model orders action i before action j.
        throw new IllegalStateException("TODO: happensBefore");
    }
}
