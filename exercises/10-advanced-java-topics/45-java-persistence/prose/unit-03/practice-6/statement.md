Pessimistic locking reads a row with `SELECT ... FOR UPDATE`, which holds a
write lock until commit or rollback. Two transfers that lock A then B and B
then A can each wait for the other forever. The page's fix: **lock in a
consistent order**, lower id first.

Write `LockedTransfer.transfer(connection, fromId, toId, amount)` over
`accounts(id INT PRIMARY KEY, balance DECIMAL(15,2))`, in one transaction:

- Lock both rows with `SELECT id, balance FROM accounts WHERE id = ? FOR UPDATE`,
  **in ascending id order, whatever the direction of the transfer**.
- **A missing account on either side changes nothing**: roll back, return `false`.
- **Check the balance on the locked row**, never with a read made before the
  lock. Too little money: roll back, return `false`.
- Otherwise debit, credit, commit, return `true`. On `SQLException`, roll
  back and rethrow. Restore autoCommit to `true` at the end. The amount is
  never negative in these tests.

| balances (1, 2) | call | locks, in order | result | balances after |
|---|---|---|---|---|
| 100.00, 50.00 | `transfer(2, 1, 30.00)` | 1, 2 | `true` | 130.00, 20.00 |
| 100.00, 50.00 | `transfer(1, 99, 30.00)` | 1, 99 | `false` | 100.00, 50.00 |
| 100.00, 50.00 | `transfer(2, 1, 60.00)` | 1, 2 | `false` | 100.00, 50.00 |
| 130.00, 20.00 | `transfer(2, 1, 20.00)` | 1, 2 | `true` | 150.00, 0.00 |
