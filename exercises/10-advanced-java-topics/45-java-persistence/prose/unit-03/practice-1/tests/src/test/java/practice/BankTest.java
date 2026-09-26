package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankTest {

    private Connection connection;
    /** A second connection: it sees only what was committed. */
    private Connection observer;

    @BeforeEach
    void openDatabase() throws SQLException {
        String url = "jdbc:h2:mem:" + UUID.randomUUID();
        connection = DriverManager.getConnection(url);
        observer = DriverManager.getConnection(url);
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE accounts (id INT PRIMARY KEY, owner VARCHAR(100), balance DECIMAL(15,2))");
            statement.execute("INSERT INTO accounts VALUES (1, 'Ada', 100.00), (2, 'Linus', 50.00)");
        }
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        observer.close();
        connection.close();
    }

    private BigDecimal committedBalance(int id) throws SQLException {
        try (PreparedStatement statement = observer.prepareStatement("SELECT balance FROM accounts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getBigDecimal(1);
            }
        }
    }

    private void assertBalances(String first, String second) throws SQLException {
        assertThat(committedBalance(1)).isEqualByComparingTo(first);
        assertThat(committedBalance(2)).isEqualByComparingTo(second);
    }

    @Test
    void movesMoneyAndCommits() throws SQLException {
        assertThat(Bank.transfer(connection, 1, 2, new BigDecimal("30.00"))).isTrue();
        assertBalances("70.00", "80.00");

        assertThat(Bank.transfer(connection, 1, 2, new BigDecimal("70.00"))).isTrue();
        assertBalances("0.00", "150.00");
    }

    @Test
    void aMissingDestinationUndoesTheDebit() throws SQLException {
        assertThat(Bank.transfer(connection, 1, 99, new BigDecimal("30.00"))).isFalse();

        assertBalances("100.00", "50.00");
    }

    @Test
    void insufficientFundsChangesNothing() throws SQLException {
        assertThat(Bank.transfer(connection, 1, 2, new BigDecimal("100.01"))).isFalse();

        assertBalances("100.00", "50.00");
    }

    @Test
    void autoCommitIsRestored() throws SQLException {
        Bank.transfer(connection, 1, 2, new BigDecimal("30.00"));
        assertThat(connection.getAutoCommit()).isTrue();

        Bank.transfer(connection, 1, 99, new BigDecimal("30.00"));
        assertThat(connection.getAutoCommit()).isTrue();
    }

    @Test
    void aNegativeAmountIsRefused() throws SQLException {
        assertThatThrownBy(() -> Bank.transfer(connection, 1, 2, new BigDecimal("-5.00")))
                .isInstanceOf(IllegalArgumentException.class);

        assertBalances("100.00", "50.00");
    }
}
