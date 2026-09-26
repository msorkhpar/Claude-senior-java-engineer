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
        int n = trace.size();
        boolean[][] hb = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                hb[i][j] = i != j && edge(trace.get(i), i, trace.get(j), j);
            }
        }
        for (int k = 0; k < n; k++) {          // transitivity: close the relation
            for (int i = 0; i < n; i++) {
                if (!hb[i][k]) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (hb[k][j]) {
                        hb[i][j] = true;
                    }
                }
            }
        }
        return hb[a][b];
    }

    private static boolean edge(Action x, int i, Action y, int j) {
        if (x.thread().equals(y.thread())) {
            return i < j;                                   // program order
        }
        if (x.kind() == Kind.START && x.target().equals(y.thread())) {
            return true;                                    // thread start
        }
        if (y.kind() == Kind.JOIN && y.target().equals(x.thread())) {
            return i < j;                                   // thread join
        }
        if (i > j) {
            return false;
        }
        if (x.kind() == Kind.UNLOCK && y.kind() == Kind.LOCK) {
            return true;                                    // any unlock publishes
        }
        if (x.kind() == Kind.VOLATILE_WRITE && y.kind() == Kind.VOLATILE_READ) {
            return x.target().equals(y.target());           // volatile variable
        }
        return false;
    }
}
