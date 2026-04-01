# 10.7.3. Transactions and Database Concurrency Control

## Concept Explanation

A **transaction** is a sequence of one or more database operations that are treated as a single logical unit of work. Either all operations in the transaction complete successfully (commit), or none of them take effect (rollback). Transactions are the cornerstone of data integrity in any system that modifies persistent state.

**Real-world analogy**: Consider a bank transfer of $500 from Account A to Account B. This involves two operations: debit A by $500 and credit B by $500. If the system crashes after the debit but before the credit, the $500 has vanished. A transaction wraps both operations into one atomic unit: either both happen, or neither does. The bank's ledger is never in an inconsistent state.

### ACID Properties

Every transaction must satisfy four properties, collectively known as ACID:

1. **Atomicity**: All operations in a transaction succeed or all fail. There is no partial execution. Implemented by the database's undo/redo log.

2. **Consistency**: A transaction takes the database from one valid state to another. All constraints (primary keys, foreign keys, check constraints, triggers) are satisfied after the transaction commits.

3. **Isolation**: Concurrent transactions behave as though they execute sequentially. The degree of isolation is configurable through isolation levels.

4. **Durability**: Once a transaction commits, its changes survive any subsequent crash. Implemented by writing to persistent storage (WAL - Write-Ahead Log).

### Transaction Isolation Levels

The SQL standard defines four isolation levels that trade off between concurrency and correctness:

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read |
|---|---|---|---|
| READ UNCOMMITTED | Possible | Possible | Possible |
| READ COMMITTED | Prevented | Possible | Possible |
| REPEATABLE READ | Prevented | Prevented | Possible |
| SERIALIZABLE | Prevented | Prevented | Prevented |

**Dirty Read**: Transaction A reads data written by Transaction B before B commits. If B rolls back, A has read data that never existed.

**Non-Repeatable Read**: Transaction A reads a row, Transaction B modifies and commits it, Transaction A reads the same row again and gets different values.

**Phantom Read**: Transaction A reads rows matching a condition, Transaction B inserts a new row matching that condition, Transaction A re-reads and sees the new "phantom" row.

### Optimistic vs. Pessimistic Locking

**Pessimistic Locking**: Assumes conflicts are likely. Acquires database locks (SELECT ... FOR UPDATE) before modifying data, blocking other transactions from reading or writing the locked rows. Best for high-contention scenarios.

**Optimistic Locking**: Assumes conflicts are rare. Does not acquire locks during reads. Instead, uses a version number or timestamp. At write time, checks that the version has not changed since the read. If it has, the write fails. Best for low-contention, read-heavy scenarios.

## Key Points to Remember

- In JDBC, transactions begin implicitly when `autoCommit` is set to `false`. They end with `commit()` or `rollback()`.
- `Connection.setAutoCommit(false)` starts manual transaction management. Each statement is no longer a separate transaction.
- **Savepoints** allow partial rollback within a transaction: you can roll back to a named savepoint without aborting the entire transaction.
- The default isolation level for most databases is READ COMMITTED (PostgreSQL, Oracle) or REPEATABLE READ (MySQL/InnoDB).
- Pessimistic locks are released when the transaction commits or rolls back.
- Optimistic locking via `@Version` in JPA increments a version column on every UPDATE. A stale version causes `OptimisticLockException`.
- **Deadlocks** occur when two transactions each hold a lock the other needs. Prevention: always acquire locks in a consistent order.
- **Lock timeout**: Set a timeout to avoid indefinite blocking: `SET LOCK_TIMEOUT 5000` (database-specific).
- In distributed systems, use **two-phase commit (2PC)** or **saga pattern** for transactions spanning multiple databases.

## Relevant Java 21 Features

- **Virtual threads** (Java 21) are significant for transaction-heavy applications. Each virtual thread can hold a database connection and transaction without consuming a platform thread, enabling millions of concurrent transactions.
- **Structured concurrency** (Java 21 preview) helps manage multiple transactional operations that run in parallel, ensuring they all complete or all fail.
- **Records** are ideal for representing immutable snapshots of versioned entities used in optimistic locking comparisons.
- **Pattern matching for switch** can be used to handle different transaction outcomes elegantly.

```java
// Using virtual threads for concurrent transaction processing (Java 21)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Boolean>> results = new ArrayList<>();
    for (TransferRequest request : requests) {
        results.add(executor.submit(() ->
            transactionService.transfer(request.fromId(), request.toId(), request.amount())
        ));
    }
    // Each virtual thread holds its own connection and transaction
}
```

## Common Pitfalls and How to Avoid Them

### 1. Forgetting to Restore AutoCommit

```java
// WRONG: autoCommit stays false after the method returns
Connection conn = pool.getConnection();
conn.setAutoCommit(false);
// ... do work ...
conn.commit();
conn.close(); // Connection returned to pool with autoCommit=false!

// CORRECT: always restore autoCommit in a finally block
Connection conn = pool.getConnection();
try {
    conn.setAutoCommit(false);
    // ... do work ...
    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
    conn.close();
}
```

### 2. Deadlocks from Inconsistent Lock Ordering

```java
// WRONG: Thread 1 locks A then B, Thread 2 locks B then A -> deadlock
// Thread 1: SELECT * FROM accounts WHERE id=1 FOR UPDATE;
//           SELECT * FROM accounts WHERE id=2 FOR UPDATE;
// Thread 2: SELECT * FROM accounts WHERE id=2 FOR UPDATE;
//           SELECT * FROM accounts WHERE id=1 FOR UPDATE;

// CORRECT: always lock in a consistent order (e.g., by ascending ID)
int firstId = Math.min(fromId, toId);
int secondId = Math.max(fromId, toId);
lockAccount(conn, firstId);
lockAccount(conn, secondId);
```

### 3. Long-Running Transactions

```java
// WRONG: holding a transaction open while waiting for user input
conn.setAutoCommit(false);
Account account = loadAccount(conn, id); // locks held
displayToUser(account);                   // may take minutes
String input = readUserInput();           // blocks!
updateAccount(conn, account, input);
conn.commit();                            // locks held for minutes

// CORRECT: read without locks, then open a short transaction for the write
Account account = loadAccount(id);           // no transaction
displayToUser(account);
String input = readUserInput();
conn.setAutoCommit(false);
updateAccountWithOptimisticLock(conn, account, input); // short transaction
conn.commit();
```

### 4. Swallowing Rollback Exceptions

```java
// WRONG: rollback exception hides the original error
try {
    conn.setAutoCommit(false);
    // ... operations that throw ...
    conn.commit();
} catch (SQLException e) {
    conn.rollback(); // if this throws, original exception is lost!
    throw e;
}

// CORRECT: handle rollback failure separately
} catch (SQLException e) {
    try {
        conn.rollback();
    } catch (SQLException rollbackEx) {
        e.addSuppressed(rollbackEx);
    }
    throw e;
}
```

### 5. Using Wrong Isolation Level

```java
// WRONG: using SERIALIZABLE everywhere (causes excessive locking and deadlocks)
conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

// CORRECT: use the minimum isolation level that meets your consistency requirements
// For most operations: READ_COMMITTED is sufficient
conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

// Only escalate to SERIALIZABLE for operations requiring it
// (e.g., checking a constraint then inserting based on the check)
```

## Best Practices and Optimization Techniques

1. **Keep transactions as short as possible**. Do all preparation and validation before starting the transaction, execute only the database operations inside it.
2. **Use optimistic locking by default** for web applications. Most requests do not conflict, and optimistic locking avoids lock contention.
3. **Use pessimistic locking** only for high-contention operations like inventory decrements or seat reservations.
4. **Set appropriate lock timeouts** to avoid indefinite blocking. In JDBC: `stmt.setQueryTimeout(5)`.
5. **Acquire locks in a consistent global order** to prevent deadlocks (e.g., always lock by ascending primary key).
6. **Use savepoints** for complex transactions where partial rollback is acceptable (e.g., bulk imports where some rows may fail).
7. **Choose the right isolation level**: READ COMMITTED for most OLTP, REPEATABLE READ for read-consistency within a transaction, SERIALIZABLE only when required.
8. **Prefer application-level retries for optimistic lock failures** rather than blocking at the database level.
9. **Monitor deadlocks and lock waits** using database tools (pg_stat_activity, SHOW ENGINE INNODB STATUS).
10. **Use read replicas** for read-heavy workloads to reduce contention on the primary database.

## Edge Cases and Their Handling

1. **Connection failure during commit**: The transaction may or may not have committed. Use idempotency keys or check the result after reconnecting.
2. **Savepoint after commit/rollback**: Throws `SQLException`. Only create savepoints within an active transaction.
3. **Nested transactions**: JDBC does not support true nested transactions. Use savepoints to simulate partial rollback.
4. **Autocommit with DDL**: Many databases auto-commit before and after DDL statements (CREATE TABLE, ALTER TABLE) regardless of autocommit setting.
5. **Connection pool and transaction state**: Always restore autoCommit and clear warnings before returning a connection to the pool.
6. **Optimistic lock with delete**: If another transaction deletes the row, the version-check UPDATE matches zero rows - same as a version conflict.
7. **Read-only transactions**: Mark transactions as read-only (`conn.setReadOnly(true)`) for optimization; the database can skip undo log entries.

## Interview-specific Insights

Interviewers commonly focus on:

- **ACID properties** - be able to explain each with a concrete example (bank transfer is the classic)
- **Isolation levels** - know the phenomena each prevents (dirty reads, non-repeatable reads, phantoms)
- **Optimistic vs. pessimistic locking** - when to use each, implementation details, trade-offs
- **Deadlock prevention** - consistent lock ordering, lock timeouts
- **Transaction boundaries in Spring/JPA** - `@Transactional` annotation, propagation levels
- **Two-phase commit** - how distributed transactions work across multiple databases

Tricky areas:
- "What is the default isolation level in PostgreSQL vs. MySQL?" (READ COMMITTED vs. REPEATABLE READ)
- "Can a dirty read occur at READ COMMITTED?" (No, that is precisely what READ COMMITTED prevents)
- "How does `@Version` work in JPA?" (Auto-incremented on UPDATE, stale version causes exception)
- "What happens if two transactions both read the same row and try to update it with optimistic locking?" (Second one fails with OptimisticLockException)

## Interview Q&A Section

**Q1: Explain the ACID properties with a practical example.**

```text
A1: ACID properties ensure reliable transaction processing. Consider a bank transfer of $500
from Account A (balance $1000) to Account B (balance $200):

Atomicity: The transfer involves two operations: debit A by $500 and credit B by $500.
Either BOTH happen or NEITHER happens. If the system crashes after debiting A but before
crediting B, the transaction is rolled back and A's balance returns to $1000.

Consistency: Before the transfer, total balance = $1200. After the transfer, A=$500, B=$700,
total=$1200. The invariant "total balance is preserved" is maintained. Any database constraints
(e.g., balance >= 0) are satisfied after the transaction.

Isolation: If a concurrent transaction tries to read A's balance during the transfer, it sees
either the pre-transfer or post-transfer state, never the intermediate state (A debited but B
not yet credited). The exact behavior depends on the isolation level.

Durability: Once the transfer commits and the application receives confirmation, the new
balances ($500 and $700) survive any subsequent power failure, crash, or hardware fault.
The database writes to persistent storage (WAL/redo log) before confirming the commit.
```

```java
// ACID-compliant transfer in JDBC
public boolean transfer(Connection conn, int fromId, int toId, double amount)
        throws SQLException {
    conn.setAutoCommit(false); // Begin transaction (Atomicity boundary)
    try {
        // Debit source account
        PreparedStatement debit = conn.prepareStatement(
            "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?");
        debit.setDouble(1, amount);
        debit.setInt(2, fromId);
        debit.setDouble(3, amount); // Consistency: enforce balance >= 0
        if (debit.executeUpdate() == 0) {
            conn.rollback(); // Atomicity: undo if constraint violated
            return false;
        }

        // Credit destination account
        PreparedStatement credit = conn.prepareStatement(
            "UPDATE accounts SET balance = balance + ? WHERE id = ?");
        credit.setDouble(1, amount);
        credit.setInt(2, toId);
        credit.executeUpdate();

        conn.commit(); // Durability: changes persisted to disk
        return true;
    } catch (SQLException e) {
        conn.rollback(); // Atomicity: undo all changes on error
        throw e;
    }
}
```

**Q2: What are the differences between the four transaction isolation levels?**

```text
A2: Transaction isolation levels control the visibility of changes between concurrent transactions.
Each level prevents certain concurrency phenomena:

READ UNCOMMITTED (lowest isolation):
- Allows dirty reads: you can see uncommitted changes from other transactions.
- Rarely used in practice (only for approximate analytics where precision doesn't matter).
- Very high concurrency, very low consistency.

READ COMMITTED (default for PostgreSQL, Oracle):
- Prevents dirty reads. You only see data that has been committed.
- Allows non-repeatable reads: if you read the same row twice, you might get different values
  because another transaction committed a change between your two reads.
- Good balance for most OLTP applications.

REPEATABLE READ (default for MySQL/InnoDB):
- Prevents dirty reads and non-repeatable reads.
- Once you read a row, re-reading it in the same transaction returns the same value.
- Allows phantom reads: a range query might return different numbers of rows.
- MySQL/InnoDB actually prevents phantoms too (gap locks), exceeding the SQL standard.

SERIALIZABLE (highest isolation):
- Prevents all anomalies: dirty reads, non-repeatable reads, and phantom reads.
- Transactions behave as if they executed one at a time.
- Lowest concurrency, highest consistency.
- Implemented via range locks or serializable snapshot isolation (SSI).
```

```java
// Setting isolation levels in JDBC
Connection conn = dataSource.getConnection();

// READ COMMITTED - good default for most operations
conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

// REPEATABLE READ - for operations that read the same data multiple times
conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

// SERIALIZABLE - for critical operations requiring full isolation
conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

// Check what the database supports
DatabaseMetaData meta = conn.getMetaData();
boolean supportsSerializable = meta.supportsTransactionIsolationLevel(
    Connection.TRANSACTION_SERIALIZABLE);

// In Spring, set isolation per-method:
// @Transactional(isolation = Isolation.REPEATABLE_READ)
```

**Q3: When should you use optimistic locking vs. pessimistic locking?**

```text
A3: The choice depends on the expected contention level and access patterns:

Optimistic Locking:
- Best for: Low-contention, read-heavy workloads (web applications, APIs).
- How it works: Read data with a version number. On update, check that the version hasn't changed.
  If it has, throw an exception and let the application retry.
- Pros: No locks held during read, high concurrency, no deadlocks.
- Cons: Wasted work on conflict (read + compute + failed update), need retry logic.
- Implementation: JPA @Version, manual version column check in SQL.

Pessimistic Locking:
- Best for: High-contention, write-heavy scenarios (inventory, reservations, bidding).
- How it works: Acquire a database lock (SELECT FOR UPDATE) before reading. Other transactions
  block until the lock is released at commit/rollback.
- Pros: Guarantees success once the lock is acquired, no wasted work.
- Cons: Reduced concurrency, potential deadlocks, longer lock-hold times.
- Implementation: SELECT ... FOR UPDATE in SQL, LockModeType.PESSIMISTIC_WRITE in JPA.

Decision matrix:
- Read-heavy, few writes, web app -> Optimistic
- Write-heavy, high contention -> Pessimistic
- Long user think-time between read and write -> Optimistic (never hold DB locks while waiting for users)
- Short automated transactions -> Pessimistic is acceptable
- Distributed systems -> Optimistic (distributed locks are expensive)
```

```java
// Optimistic Locking with version column
public Account updateBalanceOptimistic(Account account, double newBalance)
        throws SQLException, OptimisticLockException {
    String sql = "UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?";
    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setDouble(1, newBalance);
        pstmt.setInt(2, account.id());
        pstmt.setInt(3, account.version()); // Check version hasn't changed
        int rows = pstmt.executeUpdate();
        if (rows == 0) {
            throw new OptimisticLockException("Stale data - retry the operation");
        }
    }
    return new Account(account.id(), account.owner(), newBalance, account.version() + 1);
}

// Pessimistic Locking with SELECT FOR UPDATE
public Account lockAndUpdateBalance(Connection conn, int id, double newBalance)
        throws SQLException {
    conn.setAutoCommit(false);
    // Lock the row - other transactions will BLOCK here until we commit
    PreparedStatement lockStmt = conn.prepareStatement(
        "SELECT id, owner, balance, version FROM accounts WHERE id = ? FOR UPDATE");
    lockStmt.setInt(1, id);
    ResultSet rs = lockStmt.executeQuery();
    if (!rs.next()) throw new RuntimeException("Account not found");

    // Now we have exclusive access to this row
    PreparedStatement updateStmt = conn.prepareStatement(
        "UPDATE accounts SET balance = ? WHERE id = ?");
    updateStmt.setDouble(1, newBalance);
    updateStmt.setInt(2, id);
    updateStmt.executeUpdate();
    conn.commit(); // Release the lock
    return new Account(id, rs.getString("owner"), newBalance, rs.getInt("version"));
}
```

**Q4: What are savepoints and when would you use them?**

```text
A4: A savepoint is a named marker within a transaction that allows you to roll back to that
specific point without aborting the entire transaction. It provides sub-transaction semantics.

Use cases:
1. Bulk data imports: Save a checkpoint every N rows. If one row fails, roll back to the
   last savepoint and skip the problematic row, then continue.
2. Complex workflows with optional steps: If a bonus calculation fails for one employee,
   roll back that employee's changes but keep the others.
3. Retry logic within a transaction: Attempt an operation, rollback to savepoint on failure,
   try alternative approach.

JDBC API:
- conn.setSavepoint("name") -> creates a named savepoint
- conn.rollback(savepoint) -> rolls back to that savepoint (transaction still active)
- conn.releaseSavepoint(savepoint) -> frees the savepoint (optional optimization)
- conn.commit() -> commits everything (all savepoints are released)
- conn.rollback() -> rolls back the entire transaction (all savepoints are lost)

Important: Savepoints are only valid within a transaction (autoCommit = false).
After commit or full rollback, all savepoints are invalidated.
```

```java
// Using savepoints for batch processing with error tolerance
public List<Integer> importEmployees(List<Employee> employees) throws SQLException {
    List<Integer> successfulIds = new ArrayList<>();
    Connection conn = dataSource.getConnection();
    conn.setAutoCommit(false);

    try {
        for (Employee emp : employees) {
            Savepoint sp = conn.setSavepoint("emp_" + emp.getId());
            try {
                insertEmployee(conn, emp);
                successfulIds.add(emp.getId());
            } catch (SQLException e) {
                // Roll back just this employee, continue with others
                conn.rollback(sp);
                log.warn("Failed to import employee {}: {}", emp.getId(), e.getMessage());
            }
        }
        conn.commit(); // Commit all successful imports
    } catch (SQLException e) {
        conn.rollback(); // Full rollback on unexpected error
        throw e;
    } finally {
        conn.setAutoCommit(true);
        conn.close();
    }
    return successfulIds;
}
```

**Q5: How does Spring's @Transactional annotation work under the hood?**

```text
A5: Spring's @Transactional annotation uses AOP (Aspect-Oriented Programming) proxies to manage
transaction boundaries declaratively. Here's what happens at runtime:

1. Spring creates a proxy (JDK dynamic proxy or CGLIB) around the target bean.
2. When a @Transactional method is called externally:
   a. The proxy intercepts the call.
   b. It obtains a Connection from the DataSource.
   c. It sets autoCommit to false (begins transaction).
   d. It binds the Connection to the current thread (ThreadLocal).
   e. The actual method executes. Any JDBC/JPA operations use the bound Connection.
   f. If the method returns normally: proxy calls commit().
   g. If a RuntimeException is thrown: proxy calls rollback().
   h. If a checked exception is thrown: proxy calls commit() (unless rollbackFor is specified).
   i. The Connection is unbound and returned to the pool.

Key attributes:
- propagation: REQUIRED (default), REQUIRES_NEW, SUPPORTS, etc.
- isolation: READ_COMMITTED (default), REPEATABLE_READ, etc.
- readOnly: Optimization hint (default false).
- rollbackFor: Which exception types trigger rollback.
- timeout: Transaction timeout in seconds.

Critical gotcha: Self-invocation (calling a @Transactional method from within the same class)
bypasses the proxy, so the transaction is NOT applied. Use a separate bean or self-inject.
```

```java
// Spring @Transactional example
@Service
public class AccountService {

    @Transactional // Default: REQUIRED propagation, READ_COMMITTED isolation
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);
        // No explicit commit needed - Spring proxy handles it
        // If any RuntimeException is thrown, Spring automatically rolls back
    }

    @Transactional(
        propagation = Propagation.REQUIRES_NEW,
        isolation = Isolation.REPEATABLE_READ,
        timeout = 10,
        rollbackFor = {BusinessException.class} // Rollback on this checked exception too
    )
    public void criticalUpdate(Long accountId, BigDecimal amount) {
        // Runs in its own transaction, isolated from the caller's transaction
    }

    @Transactional(readOnly = true) // Optimization: no undo logging needed
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
            .map(this::toDto)
            .toList();
    }
}

// GOTCHA: self-invocation bypasses the proxy
@Service
public class BadExample {
    @Transactional
    public void methodA() {
        methodB(); // Transaction is NOT applied to methodB!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void methodB() {
        // This runs in methodA's transaction, not a new one
    }
}
```

**Q6: How do you prevent deadlocks in database transactions?**

```text
A6: Deadlocks occur when two or more transactions hold locks that the other needs, creating a
circular wait. Prevention strategies:

1. Consistent lock ordering:
   Always acquire locks in the same global order (e.g., by ascending primary key).
   If Transaction A needs rows 1 and 5, always lock 1 first, then 5.

2. Short transactions:
   Minimize the time between acquiring the first lock and committing.
   Do computation before the transaction, not during it.

3. Lock timeouts:
   Set a maximum wait time for acquiring locks.
   JDBC: stmt.setQueryTimeout(5);
   Database: SET lock_timeout = 5000; (PostgreSQL)

4. Avoid lock escalation:
   Lock only the specific rows you need, not entire tables.
   Use narrow WHERE clauses in SELECT FOR UPDATE.

5. Use optimistic locking:
   Eliminates deadlocks entirely since no locks are held during reads.

6. Index your WHERE clauses:
   Without an index, some databases lock the entire table instead of specific rows.

7. Deadlock detection and retry:
   Most databases detect deadlocks and abort one transaction.
   Implement retry logic in the application (with backoff).

MySQL: SHOW ENGINE INNODB STATUS (shows recent deadlocks)
PostgreSQL: log_lock_waits = on (logs long lock waits)
```

```java
// Strategy 1: Consistent lock ordering to prevent deadlocks
public boolean transfer(int fromId, int toId, double amount) throws SQLException {
    Connection conn = dataSource.getConnection();
    conn.setAutoCommit(false);
    try {
        // ALWAYS lock lower ID first to prevent deadlock
        int firstLock = Math.min(fromId, toId);
        int secondLock = Math.max(fromId, toId);

        lockAccount(conn, firstLock);
        lockAccount(conn, secondLock);

        // Now safe to proceed - no deadlock possible
        debit(conn, fromId, amount);
        credit(conn, toId, amount);
        conn.commit();
        return true;
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    }
}

// Strategy 7: Deadlock detection with retry
public void executeWithRetry(Runnable transactionalWork, int maxRetries) {
    for (int attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            transactionalWork.run();
            return; // Success
        } catch (DeadlockException e) {
            if (attempt == maxRetries) throw e;
            try {
                // Exponential backoff with jitter
                long delay = (long) (Math.pow(2, attempt) * 100 + Math.random() * 100);
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }
    }
}
```

## Code Examples

- Implementation: [TransactionControl.java](src/main/java/com/github/msorkhpar/claudejavatutor/javapersistence/TransactionControl.java)
- Tests: [TransactionControlTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javapersistence/TransactionControlTest.java)
