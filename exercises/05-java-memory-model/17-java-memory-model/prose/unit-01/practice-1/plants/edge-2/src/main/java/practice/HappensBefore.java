package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
        int n = trace.size();
        List<List<Integer>> next = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            next.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            Action a = trace.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    continue;
                }
                Action b = trace.get(j);
                boolean later = j > i;
                boolean edge = (a.thread().equals(b.thread()) && j > i)
                        || (a.kind() == Kind.UNLOCK && b.kind() == Kind.LOCK && a.target().equals(b.target()))
                        || (a.kind() == Kind.VOLATILE_WRITE && b.kind() == Kind.VOLATILE_READ && later
                                && a.target().equals(b.target()))
                        || (a.kind() == Kind.START && b.thread().equals(a.target()))
                        || (b.kind() == Kind.JOIN && a.thread().equals(b.target()));
                if (edge) {
                    next.get(i).add(j);
                }
            }
        }
        boolean[] seen = new boolean[n];
        Deque<Integer> todo = new ArrayDeque<>(next.get(x));
        while (!todo.isEmpty()) {
            int i = todo.pop();
            if (i == y) {
                return true;
            }
            if (!seen[i]) {
                seen[i] = true;
                todo.addAll(next.get(i));
            }
        }
        return false;
    }
}
