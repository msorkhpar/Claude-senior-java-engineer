The page's domain-modeling example: a financial application has a sealed
`Transaction` type with exactly three kinds, `Deposit`, `Withdrawal` and
`Transfer`. `Ledger` models them as records of a sealed interface:

- `Deposit(String account, long cents)`
- `Withdrawal(String account, long cents)`
- `Transfer(String from, String to, long cents)`

Write `static long balance(String account, List<Transaction> transactions)`:
start at 0 and apply every transaction in order, as it affects `account`:

- a deposit into `account` adds its cents;
- a withdrawal from `account` takes its cents away;
- a transfer takes its cents from `from` and adds them to `to`;
- anything about other accounts does not change the balance.

Use a `switch` over the transaction; since `Transaction` is sealed, it needs
no `default`. Balances may go negative.

**Examples**

- `balance("A", [Deposit(A, 100), Withdrawal(A, 30)])` -> `70`
- `balance("B", [Transfer(A, B, 40)])` -> `40`
- `balance("A", [])` -> `0`
