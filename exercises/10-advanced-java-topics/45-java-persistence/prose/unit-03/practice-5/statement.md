Optimistic locking takes no lock when reading. Each row carries a version;
the write checks it and bumps it in the same statement:

```sql
UPDATE accounts SET balance = ?, version = version + 1 WHERE id = ? AND version = ?
```

If someone else wrote the row since you read it, the version no longer
matches, the UPDATE changes 0 rows, and you report a conflict.

Write `Accounts.update(connection, seen, newBalance)` over
`accounts(id INT PRIMARY KEY, owner VARCHAR, balance DECIMAL(15,2), version INT)`,
where `seen` is the `Account(id, owner, balance, version)` the caller read.

- **The UPDATE matches only the exact version the caller saw and increments
  it**, so a second writer holding the old version matches no row.
- **An update of 0 rows is an `Accounts.OptimisticLockException`**, whatever
  the reason: a newer version or a row someone deleted (the page's edge case 6).
- **`update()` returns the account with the new balance and version + 1**, so
  it can be passed to `update()` again.

| row before (balance, version) | call | result | row after |
|---|---|---|---|
| 100.00, 0 | `update(seen v0, 150.00)` | `Account[1, Ada, 150.00, 1]` | 150.00, 1 |
| 150.00, 1 (another writer won) | `update(seen v0, 90.00)` | `OptimisticLockException` | 150.00, 1 |
| deleted | `update(seen v0, 90.00)` | `OptimisticLockException` | - |
