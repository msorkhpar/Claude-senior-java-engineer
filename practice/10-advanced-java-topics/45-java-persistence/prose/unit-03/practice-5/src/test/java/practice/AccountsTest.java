package practice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import practice.Accounts.Account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountsTest {

    private Connection connection;

    @BeforeEach
    void openDatabase() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
        execute("CREATE TABLE accounts (id INT PRIMARY KEY, owner VARCHAR(100), balance DECIMAL(15,2), version INT NOT NULL)");
        execute("INSERT INTO accounts VALUES (1, 'Ada', 100.00, 0)");
    }

    @AfterEach
    void closeDatabase() throws SQLException {
        connection.close();
    }

    private void execute(String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private Account stored() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT id, owner, balance, version FROM accounts WHERE id = 1")) {
            rows.next();
            return new Account(rows.getInt(1), rows.getString(2), rows.getBigDecimal(3), rows.getInt(4));
        }
    }

    @Test
    void updatesTheBalanceAndTheVersion() throws Exception {
        Accounts.update(connection, stored(), new BigDecimal("150.00"));

        Account after = stored();
        assertThat(after.balance()).isEqualByComparingTo("150.00");
        assertThat(after.version()).isEqualTo(1);
    }

    @Test
    void aStaleSnapshotIsRefused() throws Exception {
        Account first = stored();
        Account second = stored();
        Accounts.update(connection, first, new BigDecimal("150.00"));

        assertThatThrownBy(() -> Accounts.update(connection, second, new BigDecimal("90.00")))
                .isInstanceOf(Accounts.OptimisticLockException.class);
        assertThat(stored().balance()).isEqualByComparingTo("150.00");
    }

    @Test
    void theReturnedSnapshotCanUpdateAgain() throws Exception {
        Account once = Accounts.update(connection, stored(), new BigDecimal("150.00"));
        Account twice = Accounts.update(connection, once, new BigDecimal("175.25"));

        assertThat(twice.version()).isEqualTo(2);
        assertThat(twice.balance()).isEqualByComparingTo("175.25");
        assertThat(stored().balance()).isEqualByComparingTo("175.25");
    }

    @Test
    void aDeletedRowIsAConflictToo() throws Exception {
        Account seen = stored();
        execute("DELETE FROM accounts WHERE id = 1");

        assertThatThrownBy(() -> Accounts.update(connection, seen, new BigDecimal("90.00")))
                .isInstanceOf(Accounts.OptimisticLockException.class);
    }
}
