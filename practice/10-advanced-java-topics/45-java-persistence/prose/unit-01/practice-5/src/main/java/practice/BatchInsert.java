package practice;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class BatchInsert {

    private BatchInsert() {
    }

    /** Inserts every name into people(name), chunkSize rows per batch; returns the rows inserted. */
    public static int insertAll(Connection connection, List<String> names, int chunkSize) throws SQLException {
        throw new UnsupportedOperationException("TODO");
    }
}
