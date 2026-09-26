The page's fourth pitfall: `catch (SQLException e) { conn.rollback(); throw e; }`
loses the real error when `rollback()` itself throws. The fix catches the
rollback's exception and attaches it with `e.addSuppressed(...)`.

Write `Transactions.inTransaction(connection, work)`. It turns autoCommit
off, runs `work.run(connection)` and commits.

- If the work throws, roll back and rethrow the work's exception. **If the
  rollback fails too, add its exception as suppressed and throw the
  original error.**
- **An unchecked exception from the work rolls back too**, and is rethrown
  unchanged. (Careful: turning autoCommit back on commits an open transaction.)
- **`autoCommit` is restored after a failure as well** as after success.

| work | `rollback()` | thrown | rows kept |
|---|---|---|---|
| insert 1, insert 2 | - | nothing | 1, 2 |
| insert 1, throw `SQLException("insert failed")` | throws `SQLException("rollback failed")` | `insert failed`, suppressed: `rollback failed` | - |
| insert 1, throw `IllegalStateException` | works | `IllegalStateException` | none |
