package practice;

import java.sql.Connection;
import java.sql.SQLException;

public final class Transactions {

    private Transactions() {
    }

    @FunctionalInterface
    public interface Work {
        void run(Connection connection) throws SQLException;
    }

    /** Runs the work in one transaction: commit on success, roll back on any exception. */
    public static void inTransaction(Connection connection, Work work) throws SQLException {
        connection.setAutoCommit(false);
        try {
            work.run(connection);
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
