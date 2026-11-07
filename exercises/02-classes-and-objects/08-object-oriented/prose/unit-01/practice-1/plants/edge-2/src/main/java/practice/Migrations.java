package practice;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Schema migrations: one base class, and one subclass per schema change, written by the teams. */
public class Migrations {

    /** The database's record of applied versions, with a lock so two deploys never migrate at once. */
    public static final class SchemaHistory {
        private final Set<String> applied = new LinkedHashSet<>();
        private final List<String> statements = new ArrayList<>();
        private boolean locked;

        public void lock() {
            if (locked) throw new IllegalStateException("schema history is locked by another migration");
            locked = true;
        }

        public void unlock() { locked = false; }

        public boolean isLocked() { return locked; }

        public boolean isApplied(String version) { return applied.contains(version); }

        public void record(String version) { applied.add(version); }

        public List<String> appliedVersions() { return List.copyOf(applied); }

        public void execute(String sql) { statements.add(sql); }

        public List<String> statements() { return List.copyOf(statements); }
    }

    /** The class every team extends. */
    public abstract static class Migration {
        private final String version;

        protected Migration(String version) { this.version = version; }

        public String version() { return version; }

        /**
         * Brings the database up to this migration's version. Deploys call this for every
         * migration, in order, on every start.
         */
        public final void apply(SchemaHistory history) {
            if (history.isApplied(version)) return;
            history.lock();
            try {
                migrate(history);
                history.record(version);
            } finally {
                history.unlock();
            }
        }

        /** The schema change itself: the statements this migration executes. */
        public abstract void migrate(SchemaHistory history);
    }

    public static class AddEmailColumn extends Migration {
        public AddEmailColumn() { super("V7"); }

        @Override public void migrate(SchemaHistory history) {
            history.execute("ALTER TABLE customer ADD email VARCHAR(320)");
        }
    }

    public static class IndexEmail extends Migration {
        public IndexEmail() { super("V8"); }

        @Override public void migrate(SchemaHistory history) {
            history.execute("CREATE INDEX customer_email ON customer(email)");
        }
    }
}
