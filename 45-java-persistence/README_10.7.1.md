# 10.7.1. JDBC (Java Database Connectivity)

## Concept Explanation

JDBC (Java Database Connectivity) is the standard Java API for connecting to and interacting with relational databases. It provides a vendor-neutral interface that allows Java applications to execute SQL statements, retrieve results, and manage database connections regardless of the underlying database engine. JDBC sits between application code and database-specific drivers, forming the foundation upon which higher-level persistence frameworks like JPA and Hibernate are built.

**Real-world analogy**: Think of JDBC like a universal power adapter. Your application is the device and the database is the wall outlet. Different countries (database vendors) have different outlets, but a universal adapter (JDBC) lets your device plug into any of them through a standard interface. The adapter's internal wiring (the JDBC driver) translates between your standard plug and the local socket format.

### JDBC Architecture

The JDBC architecture consists of four key components:

1. **JDBC API** (`java.sql` and `javax.sql` packages) - The programming interface applications use.
2. **JDBC Driver Manager** (`DriverManager`) - Manages a list of database drivers and establishes connections.
3. **JDBC Driver** - A vendor-specific implementation (e.g., `org.h2.Driver`, `com.mysql.cj.jdbc.Driver`).
4. **DataSource** - A factory for connections, preferred in production over `DriverManager` (supports pooling).

### Core JDBC Workflow

1. Load the driver (automatic since JDBC 4.0 via `ServiceLoader`)
2. Obtain a `Connection` from `DriverManager` or `DataSource`
3. Create a `Statement` or `PreparedStatement`
4. Execute SQL and process `ResultSet`
5. Close resources (connection, statement, result set) - best done with try-with-resources

## Key Points to Remember

- JDBC drivers are loaded automatically since JDBC 4.0 (Java 6) through the `ServiceLoader` mechanism; `Class.forName()` is no longer required.
- Always use `PreparedStatement` instead of `Statement` to prevent SQL injection and improve performance through statement caching.
- `ResultSet` is a cursor-based iterator - you move through rows with `next()` and read column values by name or index.
- Column indices in `ResultSet` are **1-based**, not 0-based.
- `ResultSet.wasNull()` must be checked after reading primitive types to detect SQL NULL values.
- Always close JDBC resources in reverse order of creation; use try-with-resources for automatic cleanup.
- `Connection.setAutoCommit(false)` starts a manual transaction; call `commit()` or `rollback()` explicitly.
- Batch operations (`addBatch()`/`executeBatch()`) dramatically reduce round trips for bulk inserts.
- `ResultSetMetaData` provides runtime inspection of column names, types, and sizes.

## Relevant Java 21 Features

- **Try-with-resources** (since Java 7, enhanced in Java 9) is the standard way to manage JDBC resource lifecycle.
- **Records** (Java 14+) make excellent value objects for mapping `ResultSet` rows into immutable data carriers.
- **Text blocks** (Java 15+) improve SQL readability by allowing multi-line strings without concatenation.
- **Pattern matching and sealed classes** (Java 21) can be combined with result mapping for type-safe query result handling.
- **Virtual threads** (Java 21) are particularly relevant for JDBC-heavy applications, since JDBC calls are blocking I/O. Virtual threads allow scaling to thousands of concurrent database operations without dedicated platform threads.

```java
// Text blocks for SQL (Java 15+)
String sql = """
    SELECT id, name, email
    FROM employees
    WHERE department = ?
      AND active = TRUE
    ORDER BY name
    """;

// Records for row mapping (Java 14+)
record Employee(int id, String name, String email) {}
```

## Common Pitfalls and How to Avoid Them

### 1. SQL Injection via String Concatenation

```java
// WRONG: vulnerable to SQL injection
String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);

// CORRECT: use PreparedStatement with parameter binding
String sql = "SELECT * FROM users WHERE name = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, userInput);
ResultSet rs = pstmt.executeQuery();
```

### 2. Resource Leaks

```java
// WRONG: resources may leak if an exception occurs
Connection conn = DriverManager.getConnection(url);
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery("SELECT 1");
// If an exception is thrown here, nothing gets closed

// CORRECT: try-with-resources closes everything automatically
try (Connection conn = DriverManager.getConnection(url);
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT 1")) {
    while (rs.next()) {
        // process
    }
}
```

### 3. Ignoring wasNull() for Primitive Types

```java
// WRONG: getInt returns 0 for NULL, which is indistinguishable from actual 0
int age = rs.getInt("age");

// CORRECT: check wasNull() after reading
int age = rs.getInt("age");
if (rs.wasNull()) {
    // Handle the NULL case
}
```

### 4. Not Using Batch for Bulk Operations

```java
// WRONG: one round trip per insert
for (String name : names) {
    pstmt.setString(1, name);
    pstmt.executeUpdate(); // N round trips
}

// CORRECT: batch all inserts, execute once
for (String name : names) {
    pstmt.setString(1, name);
    pstmt.addBatch();
}
pstmt.executeBatch(); // 1 round trip
```

### 5. Using 0-Based Column Index

```java
// WRONG: column indices start at 1 in JDBC
String name = rs.getString(0); // throws SQLException

// CORRECT: use 1-based indices (or column names)
String name = rs.getString(1);
String name = rs.getString("name"); // preferred
```

## Best Practices and Optimization Techniques

1. **Use connection pooling** (HikariCP, Apache DBCP) in production. `DriverManager` creates a new connection each time, which is expensive.
2. **Prefer column names over indices** in `ResultSet.getXxx()` calls for readability and resilience to schema changes.
3. **Set fetch size** on `Statement` for large result sets to control memory usage: `stmt.setFetchSize(100)`.
4. **Use `PreparedStatement` for repeated queries** - the database can cache the execution plan.
5. **Close `ResultSet` and `Statement` promptly** even within a still-open `Connection` to release database cursors.
6. **Use `addBatch()`/`executeBatch()`** for bulk insert/update operations to minimize network round trips.
7. **Log SQL in development** but disable SQL logging in production for security and performance.
8. **Use `DataSource` instead of `DriverManager`** for testability and connection pooling.
9. **Handle `SQLException` chains** - call `getNextException()` to inspect chained exceptions.

## Edge Cases and Their Handling

1. **NULL values**: Use `wasNull()` after reading primitive columns. For reference types, `getString()` returns `null` directly.
2. **Empty `ResultSet`**: Always check `rs.next()` before reading - an empty result set returns `false` on the first call.
3. **Large result sets**: Set `fetchSize` to avoid loading millions of rows into memory at once.
4. **Connection timeout**: Configure connection and socket timeouts via URL parameters or `DataSource` properties.
5. **Character encoding**: Ensure the JDBC URL specifies the correct character encoding (e.g., `?useUnicode=true&characterEncoding=UTF-8` for MySQL).
6. **Auto-increment key retrieval**: Use `Statement.RETURN_GENERATED_KEYS` and `getGeneratedKeys()` after insert.
7. **Concurrent `ResultSet` access**: `ResultSet` is not thread-safe. Each thread should have its own connection and statement.

## Interview-specific Insights

Interviewers commonly focus on:

- **Statement vs. PreparedStatement vs. CallableStatement** - when to use each, performance and security differences
- **SQL injection** - how PreparedStatement prevents it at the protocol level (not just string escaping)
- **Resource management** - try-with-resources and the order of closing resources
- **Connection pooling** - why it matters, how pools like HikariCP work
- **Transaction management** - autocommit, manual transactions, savepoints
- **ResultSet navigation** - forward-only vs. scrollable, updatable result sets
- **Batch processing** - performance gains and when to use it

Tricky areas:
- "What happens if you call `rs.getInt()` on a NULL column?" (Returns 0, must check `wasNull()`)
- "Why is `PreparedStatement` faster than `Statement` for repeated queries?" (Execution plan caching)
- "How does JDBC 4.0 auto-loading of drivers work?" (`ServiceLoader` mechanism via `META-INF/services`)

## Interview Q&A Section

**Q1: What is the difference between Statement, PreparedStatement, and CallableStatement?**

```text
A1: These three interfaces represent different levels of SQL execution capability in JDBC:

1. Statement: Used for simple, one-off SQL queries without parameters. The SQL is sent as a plain
   string to the database. Vulnerable to SQL injection when user input is concatenated. The database
   parses and compiles the query fresh each time.

2. PreparedStatement (extends Statement): Used for parameterized SQL queries. Parameters are bound
   using setter methods (setString, setInt, etc.) with 1-based indices. The database can cache the
   execution plan, making repeated executions faster. It prevents SQL injection because parameters
   are sent separately from the SQL structure.

3. CallableStatement (extends PreparedStatement): Used to call stored procedures in the database.
   Supports IN, OUT, and INOUT parameters. Created via connection.prepareCall("{call procedure_name(?)}").

Rule of thumb: Always use PreparedStatement for queries with parameters. Use CallableStatement only
for stored procedures. Use Statement only for DDL or truly static queries with no user input.
```

```java
// Statement - simple, no parameters
try (Statement stmt = conn.createStatement()) {
    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM employees");
}

// PreparedStatement - parameterized, safe, efficient
try (PreparedStatement pstmt = conn.prepareStatement(
        "SELECT * FROM employees WHERE department = ? AND salary > ?")) {
    pstmt.setString(1, "Engineering");
    pstmt.setDouble(2, 80000.0);
    ResultSet rs = pstmt.executeQuery();
}

// CallableStatement - stored procedures
try (CallableStatement cstmt = conn.prepareCall("{call calculate_bonus(?, ?)}")) {
    cstmt.setInt(1, employeeId);
    cstmt.registerOutParameter(2, Types.DECIMAL);
    cstmt.execute();
    double bonus = cstmt.getDouble(2);
}
```

**Q2: How does PreparedStatement prevent SQL injection?**

```text
A2: PreparedStatement prevents SQL injection at the protocol level, not through string escaping.
When you use a PreparedStatement:

1. The SQL template (with ? placeholders) is sent to the database first during the "prepare" phase.
   The database parses and compiles the execution plan for this template.

2. Parameter values are sent separately during the "execute" phase. The database treats these values
   purely as data, never as part of the SQL syntax.

This means that even if an attacker provides input like:
   ' OR '1'='1' --
The database will search for a literal string matching that value, rather than interpreting it as SQL.

With Statement + string concatenation:
   "SELECT * FROM users WHERE name = '" + input + "'"
   becomes: SELECT * FROM users WHERE name = '' OR '1'='1' --'
   This returns all rows - a successful injection.

With PreparedStatement:
   "SELECT * FROM users WHERE name = ?"  +  parameter: "' OR '1'='1' --"
   The database looks for name literally equal to "' OR '1'='1' --" - finds nothing.

The separation of SQL structure from data is the fundamental security mechanism.
```

```java
// Demonstrating that PreparedStatement treats input as data
String maliciousInput = "'; DROP TABLE users; --";

// UNSAFE: String concatenation allows injection
String unsafeSql = "SELECT * FROM users WHERE name = '" + maliciousInput + "'";
// Becomes: SELECT * FROM users WHERE name = ''; DROP TABLE users; --'

// SAFE: PreparedStatement sends value separately from SQL
PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE name = ?");
pstmt.setString(1, maliciousInput);
// Database searches for literal string "'; DROP TABLE users; --" in name column
```

**Q3: What is the proper way to manage JDBC resources and why?**

```text
A3: JDBC resources (Connection, Statement, ResultSet) must be explicitly closed to release database
cursors, network connections, and memory. The recommended approach is try-with-resources (Java 7+).

Key principles:
1. Close in reverse order of creation: ResultSet -> Statement -> Connection
2. Each resource implements AutoCloseable, so try-with-resources handles this automatically
3. Closing a Statement automatically closes its ResultSet
4. Closing a Connection automatically closes its Statements (but relying on this is bad practice)

Why it matters:
- Database connections are expensive and limited (typically 10-100 per pool)
- Open cursors consume database server memory
- Leaked connections cause pool exhaustion and application hangs
- ResultSet holds a network socket open in many drivers

Production applications use connection pools (HikariCP) which return connections to the pool
on close() rather than actually closing the physical connection.
```

```java
// Best practice: try-with-resources with all three resources
public Optional<Employee> findById(int id) throws SQLException {
    String sql = "SELECT id, name, salary FROM employees WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, id);
        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return Optional.of(new Employee(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("salary")
                ));
            }
        }
    } // All resources closed automatically, even if an exception occurred
    return Optional.empty();
}
```

**Q4: How do you handle NULL values when reading from a ResultSet?**

```text
A4: Handling NULLs in ResultSet requires understanding two behaviors:

For reference types (getString, getObject, etc.):
- Returns Java null directly. No special handling needed.

For primitive types (getInt, getDouble, getBoolean, etc.):
- Returns the primitive's default value (0, 0.0, false) for SQL NULL.
- You MUST call wasNull() immediately after the getter to distinguish NULL from a real zero.

The wasNull() method returns true if the last column read was SQL NULL.

Alternative approach: Use getObject() which returns null for SQL NULL, then cast:
   Integer age = (Integer) rs.getObject("age"); // null if SQL NULL

With Java records and Optional, you can create null-safe mappings:
   Optional.ofNullable(rs.getString("email"))
```

```java
// Handling NULL for primitive types
public record Employee(int id, String name, Double salary, String email) {}

public Employee mapRow(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");  // Never null (primary key)

    String name = rs.getString("name");  // Returns null if SQL NULL

    // For nullable numeric columns, use wasNull()
    double salaryValue = rs.getDouble("salary");
    Double salary = rs.wasNull() ? null : salaryValue;

    // Alternatively, use getObject for nullable primitives
    Integer age = (Integer) rs.getObject("age");  // null-safe

    String email = rs.getString("email");  // null if SQL NULL

    return new Employee(id, name, salary, email);
}
```

**Q5: What are the advantages of batch processing in JDBC?**

```text
A5: Batch processing groups multiple SQL statements into a single database round trip,
providing significant performance improvements:

1. Reduced network overhead: Instead of N round trips for N statements, you have 1 round trip.
   For 10,000 inserts over a network with 1ms latency, this saves ~10 seconds.

2. Database optimization: Many databases optimize batch execution by grouping writes into
   a single transaction log flush.

3. Reduced connection holding time: The connection is occupied for a shorter total duration.

Implementation:
- Use addBatch() to queue statements, executeBatch() to send them all at once.
- executeBatch() returns an int[] with update counts per statement.
- Works with both Statement (different SQL per batch entry) and PreparedStatement (same SQL,
  different parameters).

Best practices:
- Commit in chunks (e.g., every 1000 rows) for very large batches to avoid memory issues.
- Use PreparedStatement for batch inserts (same SQL template, different parameters).
- Check return values: EXECUTE_FAILED (-3) indicates a failed statement in the batch.
- Consider rewriteBatchedStatements=true for MySQL for further optimization.
```

```java
// Batch insert with PreparedStatement
public void batchInsertEmployees(List<Employee> employees) throws SQLException {
    String sql = "INSERT INTO employees (name, email, salary) VALUES (?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        conn.setAutoCommit(false);
        int batchSize = 0;

        for (Employee emp : employees) {
            pstmt.setString(1, emp.name());
            pstmt.setString(2, emp.email());
            pstmt.setDouble(3, emp.salary());
            pstmt.addBatch();
            batchSize++;

            // Execute in chunks of 1000 to manage memory
            if (batchSize % 1000 == 0) {
                pstmt.executeBatch();
            }
        }
        pstmt.executeBatch();  // Execute remaining
        conn.commit();
    }
}
```

**Q6: How does connection pooling work and why is it important?**

```text
A6: Connection pooling maintains a cache of database connections that can be reused across
requests, avoiding the expensive overhead of creating new connections.

How it works:
1. At startup, the pool creates a minimum number of connections (minIdle).
2. When code calls dataSource.getConnection(), the pool hands out an idle connection.
3. When code calls connection.close(), the pool returns it to the idle set (does not close it).
4. The pool manages max connections, idle timeouts, and connection validation.

Why it matters:
- Creating a TCP connection + database authentication takes 5-50ms per connection.
- For an application handling 1000 requests/second, this would add 5-50 seconds of overhead per second.
- Without pooling, you would quickly exhaust the database's connection limit.

Popular pools: HikariCP (fastest, Spring Boot default), Apache DBCP2, c3p0.

Key configuration parameters:
- maximumPoolSize: Max connections (typically 10-20 for most apps)
- minimumIdle: Min idle connections kept ready
- connectionTimeout: Max wait time for a connection from the pool
- maxLifetime: Max lifetime of a connection before it is retired
- idleTimeout: How long an idle connection stays in the pool
```

```java
// Using HikariCP connection pool (production-ready)
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPoolExample {
    private static HikariDataSource createPool() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("pass");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000); // 30 seconds
        config.setIdleTimeout(600000);      // 10 minutes
        config.setMaxLifetime(1800000);     // 30 minutes
        return new HikariDataSource(config);
    }

    // Usage: pool returns existing connection, close() returns it to pool
    public void queryWithPool(HikariDataSource pool) throws SQLException {
        try (Connection conn = pool.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            pstmt.setInt(1, 42);
            try (ResultSet rs = pstmt.executeQuery()) {
                // process results
            }
        } // Connection returned to pool, not closed
    }
}
```

**Q7: What is the difference between a scrollable and forward-only ResultSet?**

```text
A7: ResultSet types control how you can navigate through query results:

1. TYPE_FORWARD_ONLY (default):
   - Can only call next() to move forward through rows.
   - Most memory-efficient; rows may be streamed from the database.
   - Suitable for 99% of use cases.

2. TYPE_SCROLL_INSENSITIVE:
   - Can move forward, backward, and to absolute/relative positions.
   - A snapshot of the data is taken at query time; changes by other transactions are not visible.
   - Uses more memory (may cache entire result).

3. TYPE_SCROLL_SENSITIVE:
   - Can navigate like SCROLL_INSENSITIVE.
   - Reflects changes made by other transactions while the ResultSet is open.
   - Rarely used; performance and support varies by driver.

Navigation methods for scrollable: previous(), first(), last(), absolute(row), relative(rows).

ResultSet concurrency:
- CONCUR_READ_ONLY (default): Read-only view of results.
- CONCUR_UPDATABLE: Allows in-place updates via updateXxx() and updateRow().
```

```java
// Forward-only (default, most efficient)
try (Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {
    while (rs.next()) {
        System.out.println(rs.getString("name"));
    }
}

// Scrollable, read-only
try (Statement stmt = conn.createStatement(
        ResultSet.TYPE_SCROLL_INSENSITIVE,
        ResultSet.CONCUR_READ_ONLY);
     ResultSet rs = stmt.executeQuery("SELECT * FROM employees")) {

    rs.last();                          // Move to last row
    int totalRows = rs.getRow();        // Get row count

    rs.absolute(5);                     // Jump to row 5
    System.out.println(rs.getString("name"));

    rs.previous();                      // Move backward
    System.out.println(rs.getString("name"));
}
```

## Code Examples

- Implementation: [JdbcOperations.java](src/main/java/com/github/msorkhpar/claudejavatutor/javapersistence/JdbcOperations.java)
- Tests: [JdbcOperationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javapersistence/JdbcOperationsTest.java)
