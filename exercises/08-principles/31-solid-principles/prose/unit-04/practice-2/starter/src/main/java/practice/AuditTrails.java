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
            // TODO: Twelve teams implement AuditTrail, written before this method existed. Their
            //       classes must keep compiling, and must record a batch correctly, unchanged.
            throw new IllegalStateException("TODO: AuditTrail.appendAll");
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
        public void appendAll(List<String> batch) {
            // TODO: A payment writes its debit and its credit as one batch. The compliance report,
            //       reading entries() on its own thread, must never see one without the other.
            throw new IllegalStateException("TODO: LockedTrail.appendAll");
        }
    }
}
