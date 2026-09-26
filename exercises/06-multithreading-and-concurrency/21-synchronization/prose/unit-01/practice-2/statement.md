The page's classic deadlock: thread A runs `synchronized(account1) {
synchronized(account2) { ... } }` while thread B runs the same with the accounts
swapped. Each holds one lock and waits forever for the other. Its fix is **lock
ordering**: every transfer takes the two locks in one global order, such as by
account id.

Write `Bank.transfer(Account from, Account to, int amount, Runnable holdingFirst)`:

- It locks **both** accounts (synchronize on the `Account` objects), and moves
  `amount` from `from` to `to`, returning `true`.
- Call `holdingFirst.run()` exactly once, right after you hold the **first** of
  the two locks and before you take the second. The tests use it to line two
  transfers up.
- A transfer larger than `from`'s balance is refused: return `false` and change
  nothing.
- A transfer from an account to itself is refused: return `false`.
- Two transfers in opposite directions, run at the same time, must both finish.

`Account(long id, int balance)` is given: `id()` and `balance()` read it.

| accounts | call | answer |
|---|---|---|
| `a = (1, 100)`, `b = (2, 50)` | `transfer(a, b, 30, hook)` | `true`; `a` has 70, `b` has 80 |
| `a = (1, 10)`, `b = (2, 50)` | `transfer(a, b, 30, hook)` | `false`; nothing changes |
| `a = (1, 100)` | `transfer(a, a, 10, hook)` | `false` |
| `a = (1, 100)`, `b = (2, 50)` | `transfer(a, b, 30, hook)` and `transfer(b, a, 20, hook)` at once | both `true`; `a` has 90, `b` has 60 |
