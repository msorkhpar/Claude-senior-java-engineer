package practice;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class DumpReader {

    private DumpReader() {
    }

    /** One thread of a dump: the lock it waits for and who holds it, both null when it waits for none. */
    public record Snapshot(String name, Thread.State state, String lock, String lockOwner) {
    }

    /** How many threads are in each of the six states. */
    public static Map<Thread.State, Integer> countByState(List<Snapshot> dump) {
        throw new UnsupportedOperationException("write countByState");
    }

    /** The lock the most BLOCKED threads wait for, alphabetically first on a tie. */
    public static Optional<String> hottestMonitor(List<Snapshot> dump) {
        throw new UnsupportedOperationException("write hottestMonitor");
    }

    /** The sorted names of the threads on a cycle of BLOCKED threads, each waiting for a lock the next holds. */
    public static List<String> deadlocked(List<Snapshot> dump) {
        throw new UnsupportedOperationException("write deadlocked");
    }
}
