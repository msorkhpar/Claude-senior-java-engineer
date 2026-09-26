package practice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public final class BatchInsert {

    private BatchInsert() {
    }

    /** Inserts every name into people(name), chunkSize rows per batch; returns the rows inserted. */
    public static int insertAll(Connection connection, List<String> names, int chunkSize) throws SQLException {
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be at least 1: " + chunkSize);
        }
        int inserted = 0;
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO people (name) VALUES (?)")) {
            int queued = 0;
            for (String name : names) {
                statement.setString(1, name);
                statement.addBatch();
                queued++;
                if (queued == chunkSize) {
                    inserted += sum(statement.executeBatch());
                    queued = 0;
                }
            }
        }
        return inserted;
    }

    private static int sum(int[] counts) {
        int total = 0;
        for (int count : counts) {
            total += count;
        }
        return total;
    }
}
