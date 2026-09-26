package practice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.Isolation.Reads;

import static org.assertj.core.api.Assertions.assertThat;

class IsolationTest {

    private Connection reader;
    private Connection writer;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        reader = DriverManager.getConnection(url);
        writer = DriverManager.getConnection(url);
        try (Statement statement = writer.createStatement()) {
            statement.execute("CREATE TABLE accounts (id INT PRIMARY KEY, balance DECIMAL(15,2))");
            statement.execute("INSERT INTO accounts VALUES (1, 100.00)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        writer.close();
        reader.close();
    }

    /** The writer sets the balance to 200.00, and commits it or leaves it pending. */
    private Runnable writerSets200(boolean commit) {
        return () -> {
            try {
                writer.setAutoCommit(false);
                try (Statement statement = writer.createStatement()) {
                    statement.executeUpdate("UPDATE accounts SET balance = 200.00 WHERE id = 1");
                }
                if (commit) {
                    writer.commit();
                }
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @Test
    void readCommittedSeesACommittedChange() throws SQLException {
        Reads reads = Isolation.readTwice(reader, Connection.TRANSACTION_READ_COMMITTED, 1, writerSets200(true));

        assertThat(reads.first()).isEqualByComparingTo("100.00");
        assertThat(reads.second()).isEqualByComparingTo("200.00");
    }

    @Test
    void repeatableReadKeepsTheFirstValue() throws SQLException {
        Reads reads = Isolation.readTwice(reader, Connection.TRANSACTION_REPEATABLE_READ, 1, writerSets200(true));

        assertThat(reads.first()).isEqualByComparingTo("100.00");
        assertThat(reads.second()).isEqualByComparingTo("100.00");
    }

    @Test
    void readUncommittedSeesADirtyValue() throws SQLException {
        Reads reads = Isolation.readTwice(reader, Connection.TRANSACTION_READ_UNCOMMITTED, 1, writerSets200(false));
        writer.rollback();

        assertThat(reads.first()).isEqualByComparingTo("100.00");
        assertThat(reads.second()).isEqualByComparingTo("200.00");
    }

    @Test
    void theConnectionsSettingsAreRestored() throws SQLException {
        reader.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

        Isolation.readTwice(reader, Connection.TRANSACTION_REPEATABLE_READ, 1, writerSets200(true));

        assertThat(reader.getTransactionIsolation()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
        assertThat(reader.getAutoCommit()).isTrue();

        reader.setAutoCommit(false);
        Isolation.readTwice(reader, Connection.TRANSACTION_READ_COMMITTED, 1, () -> { });
        assertThat(reader.getAutoCommit()).isFalse();
        assertThat(reader.getTransactionIsolation()).isEqualTo(Connection.TRANSACTION_SERIALIZABLE);
    }
}
