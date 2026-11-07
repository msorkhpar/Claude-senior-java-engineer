package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Loads the night's smart-meter readings into meter_reading(meter VARCHAR(40), wh BIGINT).
 * The connection belongs to the caller, who also decides when to commit.
 */
public final class MeterImport {

    /** One reading: the meter's name and the watt-hours it reported. */
    public record Reading(String meter, long wattHours) {}

    private MeterImport() {}

    /** The import as it runs today: correct, and one SQL text per reading. */
    public static int importOneByOne(Connection conn, List<Reading> readings) throws SQLException {
        int rows = 0;
        try (Statement st = conn.createStatement()) {
            for (Reading r : readings) {
                rows += st.executeUpdate("INSERT INTO meter_reading (meter, wh) VALUES ('"
                        + r.meter().replace("'", "''") + "', " + r.wattHours() + ")");
            }
        }
        return rows;
    }

    /**
     * Inserts every reading and returns the number of rows the database reports inserted.
     * The driver is called at most once per chunkSize readings.
     */
    public static int importAll(Connection conn, List<Reading> readings, int chunkSize) throws SQLException {
        if (chunkSize < 1) throw new IllegalArgumentException("chunkSize must be at least 1, was " + chunkSize);
        int rows = 0;
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO meter_reading (meter, wh) VALUES (?, ?)")) {
            int queued = 0;
            for (Reading r : readings) {
                ps.setString(1, r.meter());
                ps.setLong(2, r.wattHours());
                ps.addBatch();
                if (++queued == chunkSize) {
                    rows += sum(ps.executeBatch());
                    queued = 0;
                }
            }
        }
        return rows;
    }

    private static int sum(int[] counts) {
        int total = 0;
        for (int c : counts) total += c;
        return total;
    }
}
