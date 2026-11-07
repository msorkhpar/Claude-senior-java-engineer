package practice;

import java.util.List;

/** Rationing for the SMS gateway: every sender asks a Quota before it sends. */
public final class Quotas {

    private Quotas() {
    }

    /** Rations a resource between callers. */
    public interface Quota {
        /**
         * Asks for {@code requested} units.
         *
         * @param requested any value from 0 up
         * @return the units granted, from 0 to {@code requested}
         */
        int grant(int requested);
    }

    /** Internal senders: everything asked for is granted. */
    public static final class Unlimited implements Quota {
        @Override
        public int grant(int requested) {
            return requested;
        }
    }

    /** A tenant's daily budget of messages. */
    public static final class DailyQuota implements Quota {
        private int remaining;

        public DailyQuota(int budget) {
            this.remaining = budget;
        }

        @Override
        public int grant(int requested) {
            if (requested <= 0) {
                throw new IllegalArgumentException("requested must be positive");
            }
            int granted = Math.min(requested, remaining);
            remaining -= granted;
            return granted;
        }

        int remaining() {
            return remaining;
        }
    }

    /** The sender every tenant's batch goes through: it sends the part of the batch it is granted. */
    public static List<String> sendable(List<String> batch, Quota quota) {
        return batch.subList(0, quota.grant(batch.size()));
    }
}
