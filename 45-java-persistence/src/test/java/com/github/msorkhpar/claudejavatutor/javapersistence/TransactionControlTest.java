package com.github.msorkhpar.claudejavatutor.javapersistence;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Transaction Control Tests")
class TransactionControlTest {

    private static final String URL = "jdbc:h2:mem:txtest;DB_CLOSE_DELAY=-1";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    private TransactionControl txControl;

    @BeforeEach
    void setUp() throws SQLException {
        txControl = new TransactionControl(URL, USER, PASSWORD);
        txControl.dropAccountsTable();
        txControl.createAccountsTable();
    }

    @AfterEach
    void tearDown() throws SQLException {
        txControl.dropAccountsTable();
    }

    @Nested
    @DisplayName("Basic Account Operations")
    class BasicAccountTests {

        @Test
        @DisplayName("Should create account and retrieve it")
        void testCreateAndFindAccount() throws SQLException {
            int id = txControl.createAccount("Alice", 1000.00);

            Optional<TransactionControl.Account> found = txControl.findAccount(id);

            assertThat(found).isPresent();
            assertThat(found.get().owner()).isEqualTo("Alice");
            assertThat(found.get().balance()).isEqualTo(1000.00);
            assertThat(found.get().version()).isZero();
        }

        @Test
        @DisplayName("Should return empty for non-existent account")
        void testFindNonExistent() throws SQLException {
            Optional<TransactionControl.Account> found = txControl.findAccount(9999);
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Atomic Transfer (ACID - Atomicity)")
    class AtomicTransferTests {

        @Test
        @DisplayName("Should transfer money between accounts atomically")
        void testSuccessfulTransfer() throws SQLException {
            int fromId = txControl.createAccount("Alice", 1000.00);
            int toId = txControl.createAccount("Bob", 500.00);

            boolean result = txControl.transfer(fromId, toId, 300.00);

            assertThat(result).isTrue();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(700.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(800.00);
        }

        @Test
        @DisplayName("Should rollback transfer when insufficient funds")
        void testInsufficientFunds() throws SQLException {
            int fromId = txControl.createAccount("Alice", 100.00);
            int toId = txControl.createAccount("Bob", 500.00);

            boolean result = txControl.transfer(fromId, toId, 200.00);

            assertThat(result).isFalse();
            // Balances should be unchanged (atomicity)
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(100.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("Should rollback when destination account does not exist")
        void testTransferToNonExistentAccount() throws SQLException {
            int fromId = txControl.createAccount("Alice", 1000.00);

            boolean result = txControl.transfer(fromId, 9999, 100.00);

            assertThat(result).isFalse();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(1000.00);
        }

        @Test
        @DisplayName("Should handle zero amount transfer")
        void testZeroAmountTransfer() throws SQLException {
            int fromId = txControl.createAccount("Alice", 1000.00);
            int toId = txControl.createAccount("Bob", 500.00);

            boolean result = txControl.transfer(fromId, toId, 0.00);

            assertThat(result).isTrue();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(1000.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("Should handle exact balance transfer")
        void testExactBalanceTransfer() throws SQLException {
            int fromId = txControl.createAccount("Alice", 500.00);
            int toId = txControl.createAccount("Bob", 0.00);

            boolean result = txControl.transfer(fromId, toId, 500.00);

            assertThat(result).isTrue();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(0.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(500.00);
        }
    }

    @Nested
    @DisplayName("Savepoint Tests")
    class SavepointTests {

        @Test
        @DisplayName("Should apply bonus to existing accounts and skip non-existent ones")
        void testApplyBonusWithSavepoints() throws SQLException {
            int id1 = txControl.createAccount("Alice", 100.00);
            int id2 = txControl.createAccount("Bob", 200.00);
            int nonExistentId = 9999;

            List<Integer> successful = txControl.applyBonusWithSavepoints(
                    List.of(id1, nonExistentId, id2), 50.00);

            assertThat(successful).containsExactly(id1, id2);
            assertThat(txControl.findAccount(id1).get().balance()).isEqualTo(150.00);
            assertThat(txControl.findAccount(id2).get().balance()).isEqualTo(250.00);
        }

        @Test
        @DisplayName("Should handle empty account list for bonus")
        void testApplyBonusEmptyList() throws SQLException {
            List<Integer> successful = txControl.applyBonusWithSavepoints(List.of(), 100.00);
            assertThat(successful).isEmpty();
        }

        @Test
        @DisplayName("Should apply bonus to all accounts when all exist")
        void testApplyBonusAllExist() throws SQLException {
            int id1 = txControl.createAccount("Alice", 100.00);
            int id2 = txControl.createAccount("Bob", 200.00);

            List<Integer> successful = txControl.applyBonusWithSavepoints(List.of(id1, id2), 25.00);

            assertThat(successful).containsExactly(id1, id2);
        }
    }

    @Nested
    @DisplayName("Transaction Isolation Levels")
    class IsolationLevelTests {

        @Test
        @DisplayName("Should read account with READ_COMMITTED isolation")
        void testReadCommittedIsolation() throws SQLException {
            int id = txControl.createAccount("Alice", 1000.00);

            Optional<TransactionControl.Account> result =
                    txControl.readWithIsolation(id, Connection.TRANSACTION_READ_COMMITTED);

            assertThat(result).isPresent();
            assertThat(result.get().balance()).isEqualTo(1000.00);
        }

        @Test
        @DisplayName("Should read account with SERIALIZABLE isolation")
        void testSerializableIsolation() throws SQLException {
            int id = txControl.createAccount("Bob", 2000.00);

            Optional<TransactionControl.Account> result =
                    txControl.readWithIsolation(id, Connection.TRANSACTION_SERIALIZABLE);

            assertThat(result).isPresent();
            assertThat(result.get().balance()).isEqualTo(2000.00);
        }

        @Test
        @DisplayName("Should return supported isolation levels")
        void testGetSupportedIsolationLevels() throws SQLException {
            List<String> levels = txControl.getSupportedIsolationLevels();
            assertThat(levels).isNotEmpty();
            // H2 supports all standard isolation levels
            assertThat(levels).contains("READ_COMMITTED", "SERIALIZABLE");
        }
    }

    @Nested
    @DisplayName("Optimistic Locking")
    class OptimisticLockTests {

        @Test
        @DisplayName("Should update balance with correct version")
        void testOptimisticLockSuccess() throws Exception {
            int id = txControl.createAccount("Alice", 1000.00);
            TransactionControl.Account account = txControl.findAccount(id).orElseThrow();

            TransactionControl.Account updated =
                    txControl.updateBalanceOptimistic(account, 1500.00);

            assertThat(updated.balance()).isEqualTo(1500.00);
            assertThat(updated.version()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw OptimisticLockException on stale version")
        void testOptimisticLockConflict() throws Exception {
            int id = txControl.createAccount("Alice", 1000.00);
            TransactionControl.Account account = txControl.findAccount(id).orElseThrow();

            // First update succeeds
            txControl.updateBalanceOptimistic(account, 1500.00);

            // Second update with stale version should fail
            assertThatThrownBy(() ->
                    txControl.updateBalanceOptimistic(account, 2000.00)
            ).isInstanceOf(TransactionControl.OptimisticLockException.class)
                    .hasMessageContaining("modified by another transaction");
        }

        @Test
        @DisplayName("Should allow sequential optimistic updates with fresh versions")
        void testSequentialOptimisticUpdates() throws Exception {
            int id = txControl.createAccount("Alice", 1000.00);

            TransactionControl.Account v0 = txControl.findAccount(id).orElseThrow();
            TransactionControl.Account v1 = txControl.updateBalanceOptimistic(v0, 1100.00);
            TransactionControl.Account v2 = txControl.updateBalanceOptimistic(v1, 1200.00);

            assertThat(v2.balance()).isEqualTo(1200.00);
            assertThat(v2.version()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Pessimistic Locking")
    class PessimisticLockTests {

        @Test
        @DisplayName("Should acquire FOR UPDATE lock and read account")
        void testFindAccountForUpdate() throws SQLException {
            int id = txControl.createAccount("Alice", 1000.00);

            try (Connection conn = txControl.getConnection()) {
                conn.setAutoCommit(false);
                Optional<TransactionControl.Account> locked = txControl.findAccountForUpdate(conn, id);

                assertThat(locked).isPresent();
                assertThat(locked.get().balance()).isEqualTo(1000.00);
                conn.commit();
            }
        }

        @Test
        @DisplayName("Should return empty for non-existent account with FOR UPDATE")
        void testFindForUpdateNonExistent() throws SQLException {
            try (Connection conn = txControl.getConnection()) {
                conn.setAutoCommit(false);
                Optional<TransactionControl.Account> locked = txControl.findAccountForUpdate(conn, 9999);

                assertThat(locked).isEmpty();
                conn.commit();
            }
        }

        @Test
        @DisplayName("Should transfer with pessimistic locking successfully")
        void testTransferWithPessimisticLock() throws SQLException {
            int fromId = txControl.createAccount("Alice", 1000.00);
            int toId = txControl.createAccount("Bob", 500.00);

            boolean result = txControl.transferWithPessimisticLock(fromId, toId, 250.00);

            assertThat(result).isTrue();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(750.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(750.00);
        }

        @Test
        @DisplayName("Should fail pessimistic transfer with insufficient funds")
        void testPessimisticTransferInsufficientFunds() throws SQLException {
            int fromId = txControl.createAccount("Alice", 50.00);
            int toId = txControl.createAccount("Bob", 500.00);

            boolean result = txControl.transferWithPessimisticLock(fromId, toId, 100.00);

            assertThat(result).isFalse();
            assertThat(txControl.findAccount(fromId).get().balance()).isEqualTo(50.00);
            assertThat(txControl.findAccount(toId).get().balance()).isEqualTo(500.00);
        }

        @Test
        @DisplayName("Should handle reverse-order account IDs in pessimistic transfer")
        void testPessimisticTransferReverseOrder() throws SQLException {
            int id1 = txControl.createAccount("Alice", 1000.00);
            int id2 = txControl.createAccount("Bob", 500.00);

            // Transfer from higher ID to lower ID (tests deadlock-prevention ordering)
            boolean result = txControl.transferWithPessimisticLock(id2, id1, 200.00);

            assertThat(result).isTrue();
            assertThat(txControl.findAccount(id1).get().balance()).isEqualTo(1200.00);
            assertThat(txControl.findAccount(id2).get().balance()).isEqualTo(300.00);
        }
    }
}
