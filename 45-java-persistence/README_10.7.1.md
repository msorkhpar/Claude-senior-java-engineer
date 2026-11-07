# 10.7.1. JDBC (Java Database Connectivity)

The nightly sensor import pastes each reading's values into a new SQL string. The payment job
failed last night on a memo that was too long for its column: the account that payment debited
stayed debited, and no payment row says where the money went. The same morning the support screen
that lists open orders started throwing `The object is already closed`. Each failure breaks one
JDBC rule about what a statement, a result set and a connection hold.

Every run on this page uses H2 2.2.224, in-memory (`jdbc:h2:mem:...`), on Java 21.

## A PreparedStatement Is Parsed Once and Bound Per Row; a Batch Sends the Rows in One Call

Bound values let the database parse a statement once and run it many times. We insert 20,000
sensor readings three ways: with the values pasted into the SQL text, with one
`PreparedStatement` executed per row, and with the same statement batched:

```java
static void textPerRow(Connection conn) throws SQLException {
    try (Statement st = conn.createStatement()) {
        for (int i = 0; i < ROWS; i++) {
            st.executeUpdate("INSERT INTO reading (sensor, celsius) VALUES ('s-" + i + "', " + (i % 40) + ")");
        }
    }
}

static void boundPerRow(Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO reading (sensor, celsius) VALUES (?, ?)")) {
        for (int i = 0; i < ROWS; i++) {
            ps.setString(1, "s-" + i);
            ps.setInt(2, i % 40);
            ps.executeUpdate();
        }
    }
}

static void batched(Connection conn) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("INSERT INTO reading (sensor, celsius) VALUES (?, ?)")) {
        for (int i = 0; i < ROWS; i++) {
            ps.setString(1, "s-" + i);
            ps.setInt(2, i % 40);
            ps.addBatch();
        }
        ps.executeBatch();
    }
}
```

Each version ran 20 times on a fresh database; we dropped the first five rounds as warm-up:

```text
text per row    median  38 ms (fastest 35, slowest 56) for 20,000 rows
bound per row   median  24 ms (fastest 20, slowest 29) for 20,000 rows
bound, batched  median  24 ms (fastest 21, slowest 36) for 20,000 rows
```

The text version is slowest because every row is a new SQL string, so the database parses 20,000
statements. A `PreparedStatement` is parsed once, and each execution only binds new values. The
same separation of text and values is what stops SQL injection (units 7.4.5 and 10.6.2).

Batching changed nothing here: H2 runs inside our JVM, so each execution is a method call.
Against a database across a network each `executeUpdate` waits for a round trip, and a batch can
send the queued rows together; this run can't measure that.

> **Drivers differ.** Whether a batch really travels in one round trip is up to the driver, and
> some send it row by row unless a connection setting asks otherwise.

`executeBatch()` returns one update count per queued statement, and a count of 0 is not an error:

```text
executeBatch() returned [1, 1, 1]
two UPDATEs batched returned [3, 0]
```

The second `UPDATE` matched no row. Code that ignores the counts doesn't notice.

<!-- practice 1 -->

## A ResultSet Lives Only as Long as Its Statement

The open-orders screen calls this method and iterates what it returns:

```java
static ResultSet openOrders(Connection conn, String customer) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(
            "SELECT id FROM orders WHERE customer = ? AND status = 'OPEN'")) {
        ps.setString(1, customer);
        return ps.executeQuery();
    }
}
```

```text
org.h2.jdbc.JdbcSQLNonTransientException: The object is already closed [90007-224]
```

A `ResultSet` belongs to the statement that produced it, wherever the driver keeps the rows. The
`Statement` javadoc says that closing a statement closes its current result set, and the
try-with-resources closed the statement on `return`, before the caller called `next()`. The
`ResultSet` javadoc adds that executing the statement again closes it too, so a nested loop that
runs its inner query on the outer loop's statement loses the outer rows:

```text
after ps.close(): rs.isClosed() = true
first ResultSet after the second executeQuery(): isClosed() = true
```

The fix is to read the rows inside the method and return a `List` of records. A try-with-resources
that opens a connection, a statement and a result set closes them in reverse order of declaration,
as a wrapper that prints each `close()` shows:

```text
read id 1
read id 3
close ResultSet
close PreparedStatement
close Connection
```

A method that is lent a connection closes only what it opened. With a connection pool,
`close()` on a borrowed connection hands it back to the pool rather than closing it; no pool
library was run for this page.

<!-- practice 2 -->

## Auto-Commit Is On, So Each Statement Commits by Itself

The payment code debits the account and records the payment with two statements:

```java
static void pay(Connection conn, int account, BigDecimal amount, String memo) throws SQLException {
    try (PreparedStatement debit = conn.prepareStatement(DEBIT);
         PreparedStatement payment = conn.prepareStatement(PAY)) {
        debit.setBigDecimal(1, amount);
        debit.setInt(2, account);
        debit.executeUpdate();
        payment.setInt(1, account);
        payment.setBigDecimal(2, amount);
        payment.setString(3, memo);
        payment.executeUpdate();
    }
}
```

The account holds 1000.00, the payment is 250.00, and the memo `rent for March, flat 4B` has 23
characters for a `VARCHAR(20)` column. Before you look: what is the balance after the exception?

```text
getAutoCommit() on a new connection: true
auto-commit: Value too long for column "MEMO CHARACTER VARYING(20)": "'rent for March, flat 4B' (23)"; SQL statement:
  balance 750.00, payments 0
```

Under auto-commit each statement is its own transaction, committed when it completes. The debit
was committed before the insert failed, and the exception undid nothing. The fix turns auto-commit
off for the unit of work, commits at the end, rolls back on any failure, and gives the connection
back in the mode it came in:

```java
static void payInTransaction(Connection conn, int account, BigDecimal amount, String memo) throws SQLException {
    boolean auto = conn.getAutoCommit();
    conn.setAutoCommit(false);
    try {
        pay(conn, account, amount, memo);
        conn.commit();
    } catch (SQLException | RuntimeException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(auto);
    }
}
```

```text
transaction: Value too long for column "MEMO CHARACTER VARYING(20)": "'rent for March, flat 4B' (23)"; SQL statement:
  balance 1000.00, payments 0
```

Under auto-commit, a batch of four payment rows whose third memo is too long:

```text
BatchUpdateException, getUpdateCounts() = [1, 1, -3, 1]
rows in payment: a b d
```

H2 ran the rest of the batch after the failed row and committed the three good ones; `-3` is
`Statement.EXECUTE_FAILED`. The `executeBatch` javadoc lets a driver stop at the first failure
instead, so the portable way to get all or nothing is a transaction around the batch.

### Spot the Problem: Restoring Auto-Commit in `finally` Commits the Half

A shorter version passed review: it turns auto-commit off, and its `finally` turns it back on. The
catch only logs.

```java
conn.setAutoCommit(false);
try {
    Commit.pay(conn, 1, new BigDecimal("250.00"), "rent for March, flat 4B");
    conn.commit();
} catch (SQLException e) {
    System.out.println("caught, no rollback");
} finally {
    conn.setAutoCommit(true);
}
```

```text
caught, no rollback
finally setAutoCommit(true): balance 750.00
```

The `setAutoCommit` javadoc says that changing the mode during a transaction commits it. The
debit was still pending when the insert failed, and the `finally` committed it. Rollback has to
come before anything that ends the transaction, which is why the fix above rolls back in the catch,
for unchecked exceptions too. Concurrent transactions are unit 10.7.3.

<!-- practice 3 -->

## The Database Assigns the Key; Ask the Statement That Inserted the Row

A ticket's id comes from an identity column, and the code needs it to insert the ticket's child
rows. Connection `a` inserts a ticket, connection `b` inserts one before `a` asks for its id, and
`a` asks the way many first versions do:

```java
insert(a, "printer jams");
insert(b, "VPN drops");
try (Statement st = a.createStatement(); ResultSet rs = st.executeQuery("SELECT MAX(id) FROM ticket")) {
    rs.next();
    System.out.println("a: SELECT MAX(id) after its insert = " + rs.getLong(1));
}
```

```text
a: SELECT MAX(id) after its insert = 2
```

That is `b`'s ticket, and `a` would attach its child rows to it. Ask the inserting statement
instead; a `PreparedStatement` must be told when it is prepared:

```java
try (PreparedStatement ps = a.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, "screen flickers");
    ps.executeUpdate();
    insert(b, "mail bounces");
    try (ResultSet keys = ps.getGeneratedKeys()) {
        keys.next();
        System.out.println("a: getGeneratedKeys() = " + keys.getLong(1));
    }
}
```

```text
a: getGeneratedKeys() = 3
without RETURN_GENERATED_KEYS, getGeneratedKeys().next() = false
batch of 3, getGeneratedKeys() = [6, 7, 8]
after a rolled-back insert, next key = 10
```

Key 4 went to `b`'s next ticket and key 5 to an insert prepared without the flag, for which H2
returned an empty result set, not an error. The batch was prepared with the column-names overload,
`prepareStatement(INSERT, new String[] {"id"})`; returning keys after a batch is H2's behaviour,
and the javadoc doesn't promise it. A rolled-back insert still used up key 9, so ids have gaps and
don't count rows.

<!-- practice 4 -->

## A Getter Converts; Ask getObject for the Java Type You Mean

A shift starts at `TIMESTAMP '2024-03-31 02:30:00'`, and its break length is `NULL`. The JVM runs in
`Europe/Berlin`, where clocks jumped from 02:00 to 03:00 that night:

```java
System.out.printf("id %d: getTimestamp %s | getObject %s | getInt %d | getObject(Integer) %s%n",
        rs.getInt(1),
        rs.getTimestamp(2).toLocalDateTime(),
        rs.getObject(2, LocalDateTime.class),
        rs.getInt(3),
        rs.getObject(3, Integer.class));
```

```text
JVM zone: Europe/Berlin
id 1: getTimestamp 2024-03-31T03:30 | getObject 2024-03-31T02:30 | getInt 0 | getObject(Integer) null
id 2: getTimestamp 2024-03-30T02:30 | getObject 2024-03-30T02:30 | getInt 0 | getObject(Integer) 0
```

A `TIMESTAMP` column has no zone. `getTimestamp` builds a `java.sql.Timestamp`, an instant, by
reading the value in the JVM's default zone, and 02:30 doesn't exist in Berlin that night, so it
came back as 03:30. The same program with `-Duser.timezone=UTC` printed
`id 1: getTimestamp 2024-03-31T02:30 | getObject 2024-03-31T02:30 | ...`: the bug hits only values
inside a DST gap of the JVM's zone. `getObject(2, LocalDateTime.class)` maps the column to the
`java.time` type with the same meaning, a date and time with no zone (unit 7.3), and
`setObject(i, localDateTime)` writes one back.

`getInt` returns `0` for SQL `NULL`, the same as row 2's real 0. Call `wasNull()` right after it,
or use `getObject(3, Integer.class)`, which returns `null`.

## Summary

- **A `PreparedStatement` is parsed once and bound per row**, which is also what keeps values out
  of the SQL; a batch saves round trips, which an in-process database doesn't have.
  An update count of 0 is a result, not an error.
- **A `ResultSet` lives only as long as its statement**: closing or re-executing the statement
  closes it, so read rows into objects before the statement closes.
- **Auto-commit is on by default**, so a failure between two statements leaves the first
  committed. Group them in a transaction, roll back in the catch, then restore the mode.
- **Ask the inserting statement for a generated key**, with `RETURN_GENERATED_KEYS` or key
  columns at prepare time; `MAX(id)` can return another session's row.
- **Getters convert**: `getTimestamp` goes through the JVM's zone and `getInt` turns `NULL` into 0;
  `getObject(i, LocalDateTime.class)` and `getObject(i, Integer.class)` don't.

## Reference

**What closes what (javadoc; H2 2.2.224 behaviour where marked):**

| Event | Effect |
|---|---|
| `Statement.close()` | closes its current `ResultSet` |
| the statement executes again | closes the `ResultSet` from the previous execution |
| try-with-resources ends | closes resources in reverse order of declaration |
| `Connection.close()` (H2) | later use of its statements and result sets throws `The object is already closed`, though their `isClosed()` still returned `false` |
| `Connection.close()` with a transaction open | result implementation-defined (javadoc); H2 rolled it back |

**Transactions and counts:**

| Call | Behaviour |
|---|---|
| `getAutoCommit()` on a new connection | `true` |
| `setAutoCommit(...)` during a transaction, mode changed | commits the transaction |
| `executeUpdate()` | the row count; `0` when the `WHERE` matched nothing |
| `executeBatch()` | one count per queued statement, or `SUCCESS_NO_INFO` (`-2`) where the driver has none |
| `BatchUpdateException.getUpdateCounts()` | H2 continued past the failed row: `EXECUTE_FAILED` (`-3`) in its place; a driver may stop instead |
| `getGeneratedKeys()` without asking at prepare time | H2: an empty result set |
| `getGeneratedKeys()` after `executeBatch()` | H2: one key per row; implementation-defined |

**Reading values:**

| Column value | Getter | Result |
|---|---|---|
| `NULL` `INT` | `getInt` | `0`; `wasNull()` is `true` right after |
| `NULL` `INT` | `getObject(i, Integer.class)` | `null` |
| `TIMESTAMP` | `getTimestamp(i)` | an instant computed in the JVM's default zone |
| `TIMESTAMP` | `getObject(i, LocalDateTime.class)` | the stored date and time, unchanged |

## Challenges

1. The import must load 2 million rows. Choose a batch size and a commit size, and say what a
   failure on row 1,500,001 leaves behind with each choice.
2. A report query returns 300,000 rows. Returning a `List` keeps them all in memory, returning the
   `ResultSet` keeps the statement open. Design a method that streams them and still closes the
   statement, and say who owns the connection while it runs.
3. Your team's schema stores times as `TIMESTAMP` and the servers run in three zones. Find every
   place that uses `getTimestamp` or `setTimestamp`, and decide whether to change the code or the
   column type to `TIMESTAMP WITH TIME ZONE`.

## Design Note: The Caller Owns the Transaction

Every method of the module's `JdbcOperations` opens its own connection with `getConnection()`,
so each call is its own transaction and no caller can make two of them succeed or fail together.
The `pay` and `payInTransaction` methods on this page take the connection as a parameter instead:
the caller decides where the unit of work begins and ends, and each method closes only what it
opened. The cost is that every method in the chain must accept the connection and must not
commit, close or change its auto-commit mode unless it set it. Unit 10.7.2 covers what an ORM
adds on top of these calls.

## Code Examples

- Implementation: [JdbcOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/javapersistence/JdbcOperations.java)
- Tests: [JdbcOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javapersistence/JdbcOperationsTest.java)
