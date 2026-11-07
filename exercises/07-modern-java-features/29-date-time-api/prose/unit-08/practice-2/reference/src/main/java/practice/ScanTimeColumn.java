package practice;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The scan-time column of a warehouse's legacy table. The column is a TIMESTAMP without a zone,
 * and holds the time the warehouse's own clocks showed at the scan. The old DAO takes and returns
 * {@code java.sql.Timestamp}, and writes the date and time a Timestamp shows through
 * {@code toLocalDateTime()}.
 */
public class ScanTimeColumn {

    private final ZoneId warehouse;

    public ScanTimeColumn(ZoneId warehouse) {
        this.warehouse = warehouse;
    }

    /** The value to hand the DAO for a scan made at {@code scanned}. */
    Timestamp toColumn(Instant scanned) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(scanned, warehouse));
    }

    /** The moment of the scan that a value read by the DAO records. */
    Instant fromColumn(Timestamp column) {
        return column.toLocalDateTime().atZone(warehouse).toInstant();
    }
}
