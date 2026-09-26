The page's bank transfer: debit A and credit B as one unit of work, so money
never vanishes halfway. In JDBC, `setAutoCommit(false)` opens the
transaction and `commit()` or `rollback()` ends it.

Write `Bank.transfer(connection, fromId, toId, amount)` over
`accounts(id INT PRIMARY KEY, owner VARCHAR, balance DECIMAL(15,2))`. The
connection is the caller's (do not close it).

- Debit with `UPDATE ... SET balance = balance - ? WHERE id = ? AND balance >= ?`:
  **the debit only happens when the balance covers the amount**. If it
  changes no row, roll back and return `false`.
- Credit the destination. If it changes no row, return `false`: **a failed
  credit rolls back the debit**.
- Otherwise commit and return `true`. On `SQLException`, roll back and rethrow.
- **`autoCommit` is restored to `true` in a `finally` block**, success or not.
- **A negative amount is refused** with `IllegalArgumentException`.

| balances (1, 2) | call | result | balances after |
|---|---|---|---|
| 100.00, 50.00 | `transfer(1, 2, 30.00)` | `true` | 70.00, 80.00 |
| 70.00, 80.00 | `transfer(1, 2, 70.00)` | `true` | 0.00, 150.00 |
| 100.00, 50.00 | `transfer(1, 99, 30.00)` | `false` | 100.00, 50.00 |
| 100.00, 50.00 | `transfer(1, 2, 100.01)` | `false` | 100.00, 50.00 |
| 100.00, 50.00 | `transfer(1, 2, -5.00)` | `IllegalArgumentException` | 100.00, 50.00 |
