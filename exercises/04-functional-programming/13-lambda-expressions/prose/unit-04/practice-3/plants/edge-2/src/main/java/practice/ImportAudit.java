package practice;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/** Audits a file import: how many of its rows the validation rejects. One instance serves every import. */
public class ImportAudit {

    /** Validates one row of an import file. */
    public interface RowCheck {
        boolean accepts(String row);
    }

    /** Streams the rows of one import file to an action, one row at a time, on the caller's thread. */
    public interface RowSource {
        void forEachRow(Consumer<String> action);
    }

    private final RowCheck check;
    private int rejectedRows;

    public ImportAudit(RowCheck check) {
        this.check = check;
    }

    /** How many rows of this import the check rejects. Several imports may be audited at the same time. */
    public int rejected(RowSource source) {
        rejectedRows = 0;
        source.forEachRow(row -> {
            if (!check.accepts(row)) rejectedRows++;
        });
        return rejectedRows;
    }
}
