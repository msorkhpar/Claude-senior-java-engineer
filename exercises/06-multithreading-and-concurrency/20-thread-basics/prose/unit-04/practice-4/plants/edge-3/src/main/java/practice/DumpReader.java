package practice;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

public final class DumpReader {

    private DumpReader() {
    }

    /** One thread of a dump: the lock it waits for and who holds it, both null when it waits for none. */
    public record Snapshot(String name, Thread.State state, String lock, String lockOwner) {
    }

    /** How many threads are in each of the six states. */
    public static Map<Thread.State, Integer> countByState(List<Snapshot> dump) {
        Map<Thread.State, Integer> counts = new EnumMap<>(Thread.State.class);
        for (Thread.State state : Thread.State.values()) {
            counts.put(state, 0);
        }
        for (Snapshot s : dump) {
            counts.merge(s.state(), 1, Integer::sum);
        }
        return counts;
    }

    /** The lock the most BLOCKED threads wait for, alphabetically first on a tie. */
    public static Optional<String> hottestMonitor(List<Snapshot> dump) {
        Map<String, Integer> waiters = new TreeMap<>();
        for (Snapshot s : dump) {
            if (s.state() == Thread.State.BLOCKED && s.lock() != null) {
                waiters.merge(s.lock(), 1, Integer::sum);
            }
        }
        String best = null;
        for (Map.Entry<String, Integer> e : waiters.entrySet()) {
            if (best == null || e.getValue() > waiters.get(best)) {
                best = e.getKey();
            }
        }
        return Optional.ofNullable(best);
    }

    /** The sorted names of the threads on a cycle of BLOCKED threads, each waiting for a lock the next holds. */
    public static List<String> deadlocked(List<Snapshot> dump) {
        Map<String, String> waitsFor = new HashMap<>();
        for (Snapshot s : dump) {
            if (s.state() == Thread.State.BLOCKED && s.lockOwner() != null) {
                waitsFor.put(s.name(), s.lockOwner());
            }
        }
        Set<String> onCycle = new HashSet<>();
        for (Map.Entry<String, String> e : waitsFor.entrySet()) {
            if (e.getKey().equals(waitsFor.get(e.getValue()))) {
                onCycle.add(e.getKey());
            }
        }
        List<String> names = new ArrayList<>(onCycle);
        names.sort(null);
        return names;
    }
}
