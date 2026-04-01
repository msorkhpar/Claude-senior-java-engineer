package com.github.msorkhpar.claudejavatutor.javapersistence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Demonstrates transaction management and database concurrency control.
 * Covers ACID properties, transaction isolation levels, and optimistic/pessimistic locking.
 */
public class TransactionControl {

    private final String url;
    private final String user;
    private final String password;

    public TransactionControl(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Record representing a bank account.
     */
    public record Account(int id, String owner, double balance, int version) {
    }

    /**
     * Creates the accounts table with a version column for optimistic locking.
     */
    public void createAccountsTable() throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS accounts (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    owner VARCHAR(100) NOT NULL,
                    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
                    version INT NOT NULL DEFAULT 0
                )
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * Drops the accounts table.
     */
    public void dropAccountsTable() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS accounts");
        }
    }

    /**
     * Inserts a new account.
     */
    public int createAccount(String owner, double initialBalance) throws SQLException {
        String sql = "INSERT INTO accounts (owner, balance, version) VALUES (?, ?, 0)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, owner);
            pstmt.setDouble(2, initialBalance);
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Finds an account by ID.
     */
    public Optional<Account> findAccount(int id) throws SQLException {
        String sql = "SELECT id, owner, balance, version FROM accounts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    // --- ACID: Atomicity demonstration ---

    /**
     * Transfers money between two accounts within a single transaction.
     * Demonstrates atomicity: either both debit and credit succeed, or neither does.
     *
     * @return true if transfer succeeded
     */
    public boolean transfer(int fromId, int toId, double amount) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Debit source
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
            try (PreparedStatement debit = conn.prepareStatement(debitSql)) {
                debit.setDouble(1, amount);
                debit.setInt(2, fromId);
                debit.setDouble(3, amount);
                int rows = debit.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false; // Insufficient funds or account not found
                }
            }

            // Credit destination
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement credit = conn.prepareStatement(creditSql)) {
                credit.setDouble(1, amount);
                credit.setInt(2, toId);
                int rows = credit.executeUpdate();
                if (rows == 0) {
                    conn.rollback();
                    return false; // Destination account not found
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // --- Savepoint demonstration ---

    /**
     * Demonstrates partial rollback using savepoints.
     * Attempts to apply a bonus to multiple accounts. If one fails, only that
     * sub-operation is rolled back, and the rest are committed.
     *
     * @return list of account IDs that successfully received the bonus
     */
    public List<Integer> applyBonusWithSavepoints(List<Integer> accountIds, double bonus) throws SQLException {
        List<Integer> successfulIds = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (int accountId : accountIds) {
                Savepoint sp = conn.setSavepoint("bonus_" + accountId);
                try {
                    String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setDouble(1, bonus);
                        pstmt.setInt(2, accountId);
                        int rows = pstmt.executeUpdate();
                        if (rows > 0) {
                            successfulIds.add(accountId);
                        } else {
                            conn.rollback(sp);
                        }
                    }
                } catch (SQLException e) {
                    conn.rollback(sp);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
        return successfulIds;
    }

    // --- Transaction Isolation Levels ---

    /**
     * Executes a read with the specified isolation level.
     */
    public Optional<Account> readWithIsolation(int id, int isolationLevel) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setTransactionIsolation(isolationLevel);
            String sql = "SELECT id, owner, balance, version FROM accounts WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapAccount(rs));
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the supported isolation levels and their names for educational purposes.
     */
    public List<String> getSupportedIsolationLevels() throws SQLException {
        List<String> levels = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED)) {
                levels.add("READ_UNCOMMITTED");
            }
            if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED)) {
                levels.add("READ_COMMITTED");
            }
            if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ)) {
                levels.add("REPEATABLE_READ");
            }
            if (meta.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE)) {
                levels.add("SERIALIZABLE");
            }
        }
        return levels;
    }

    // --- Optimistic Locking ---

    /**
     * Custom exception for optimistic locking failures.
     */
    public static class OptimisticLockException extends Exception {
        public OptimisticLockException(String message) {
            super(message);
        }
    }

    /**
     * Updates an account balance using optimistic locking (version check).
     * Mirrors JPA's @Version behavior: the UPDATE checks the current version and
     * increments it atomically. If the version has changed since read, zero rows
     * are updated and an OptimisticLockException is thrown.
     */
    public Account updateBalanceOptimistic(Account account, double newBalance)
            throws SQLException, OptimisticLockException {
        String sql = "UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, account.id());
            pstmt.setInt(3, account.version());
            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new OptimisticLockException(
                        "Account %d was modified by another transaction (expected version %d)"
                                .formatted(account.id(), account.version()));
            }
        }
        return new Account(account.id(), account.owner(), newBalance, account.version() + 1);
    }

    // --- Pessimistic Locking ---

    /**
     * Reads an account with a pessimistic write lock (SELECT ... FOR UPDATE).
     * The lock is held until the transaction commits or rolls back.
     * Must be called with autoCommit=false.
     */
    public Optional<Account> findAccountForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT id, owner, balance, version FROM accounts WHERE id = ? FOR UPDATE";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAccount(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Transfers money using pessimistic locking to prevent concurrent modifications.
     * Acquires FOR UPDATE locks on both accounts, then performs debit/credit.
     */
    public boolean transferWithPessimisticLock(int fromId, int toId, double amount) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Lock accounts in a consistent order (lower id first) to prevent deadlocks
            int firstId = Math.min(fromId, toId);
            int secondId = Math.max(fromId, toId);

            Optional<Account> firstOpt = findAccountForUpdate(conn, firstId);
            Optional<Account> secondOpt = findAccountForUpdate(conn, secondId);

            if (firstOpt.isEmpty() || secondOpt.isEmpty()) {
                conn.rollback();
                return false;
            }

            Account source = (fromId == firstId) ? firstOpt.get() : secondOpt.get();

            if (source.balance() < amount) {
                conn.rollback();
                return false;
            }

            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(debitSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, fromId);
                pstmt.executeUpdate();
            }

            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(creditSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, toId);
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    private Account mapAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("id"),
                rs.getString("owner"),
                rs.getDouble("balance"),
                rs.getInt("version")
        );
    }
}
