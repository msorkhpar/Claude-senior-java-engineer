package practice;

import java.util.ArrayList;
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
        boolean[][] hb = happensBefore(trace);
        List<int[]> races = new ArrayList<>();
        for (int i = 0; i < trace.size(); i++) {
            for (int j = i + 1; j < trace.size(); j++) {
                if (conflict(trace.get(i), trace.get(j)) && !hb[i][j] && !hb[j][i]) {
                    races.add(new int[] {i, j});
                }
            }
        }
        return races;
    }

    private static boolean plain(Action a) {
        return a.kind() == Kind.READ || a.kind() == Kind.WRITE;
    }

    private static boolean conflict(Action x, Action y) {
        return plain(x) && plain(y) && x.target().equals(y.target())
                && (x.kind() == Kind.WRITE || y.kind() == Kind.WRITE);
    }

    private static boolean[][] happensBefore(List<Action> trace) {
        int n = trace.size();
        boolean[][] hb = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                hb[i][j] = i != j && edge(trace.get(i), i, trace.get(j), j);
            }
        }
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (hb[i][k] && hb[k][j]) {
                        hb[i][j] = true;
                    }
                }
            }
        }
        return hb;
    }

    private static boolean edge(Action x, int i, Action y, int j) {
        if (x.thread().equals(y.thread())) {
            return i < j;
        }
        if (x.kind() == Kind.START && x.target().equals(y.thread())) {
            return true;
        }
        if (y.kind() == Kind.JOIN && y.target().equals(x.thread())) {
            return i < j;
        }
        if (i > j) {
            return false;
        }
        if (x.kind() == Kind.UNLOCK && y.kind() == Kind.LOCK) {
            return x.target().equals(y.target());
        }
        if (x.kind() == Kind.VOLATILE_WRITE && y.kind() == Kind.VOLATILE_READ) {
            return true;
        }
        return false;
    }
}
