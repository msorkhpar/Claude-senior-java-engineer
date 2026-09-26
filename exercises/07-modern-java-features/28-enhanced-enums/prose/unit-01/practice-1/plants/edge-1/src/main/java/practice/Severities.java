package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class Severities {

    private Severities() {
    }

    /** What every severity, from any enum, offers. */
    public interface Severity {
        String label();

        int level();
    }

    public enum StandardSeverity implements Severity {
        LOW("Low", 1), MEDIUM("Medium", 2), HIGH("High", 3);

        private final String label;
        private final int level;

        StandardSeverity(String label, int level) {
            this.label = label;
            this.level = level;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public int level() {
            return level;
        }
    }

    public enum ExtendedSeverity implements Severity {
        TRACE("Trace", 0), CRITICAL("Critical", 4), CATASTROPHIC("Catastrophic", 5);

        private final String label;
        private final int level;

        ExtendedSeverity(String label, int level) {
            this.label = label;
            this.level = level;
        }

        @Override
        public String label() {
            return label;
        }

        @Override
        public int level() {
            return level;
        }
    }

    /** Every constant of both enums, lowest level first; the caller cannot change the list. */
    public static List<Severity> all() {
        List<Severity> all = new ArrayList<>();
        Collections.addAll(all, StandardSeverity.values());
        Collections.addAll(all, ExtendedSeverity.values());
        return Collections.unmodifiableList(all);
    }

    /** The constant with the highest level among {@code among}, or empty when there is none. */
    public static Optional<Severity> highest(Collection<? extends Severity> among) {
        return among.stream().map(s -> (Severity) s).max(Comparator.comparingInt(Severity::level));
    }
}
