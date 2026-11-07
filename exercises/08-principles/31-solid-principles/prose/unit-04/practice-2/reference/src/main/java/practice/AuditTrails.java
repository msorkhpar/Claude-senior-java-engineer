package practice;

import java.util.ArrayList;
import java.util.List;

/** The audit library. Other teams implement AuditTrail too; their code is not in this file. */
public final class AuditTrails {

    private AuditTrails() {
    }

    /** Where a service records what happened, one entry per line. */
    public interface AuditTrail {
        void append(String entry);

        /** Everything recorded so far, in order, as a copy. */
        List<String> entries();

        /** New in version 2: records a batch of entries, in order. */
        default void appendAll(List<String> batch) {
            for (String entry : batch) append(entry);
        }
    }

    /**
     * The trail the payments service shares between its request threads and the compliance
     * report thread. Every method holds the trail's own lock.
     */
    public static final class LockedTrail implements AuditTrail {
        private final List<String> entries = new ArrayList<>();

        @Override
        public synchronized void append(String entry) {
            entries.add(entry);
        }

        @Override
        public synchronized List<String> entries() {
            return List.copyOf(entries);
        }

        @Override
        public synchronized void appendAll(List<String> batch) {
            for (String entry : batch) entries.add(entry);
        }
    }
}
