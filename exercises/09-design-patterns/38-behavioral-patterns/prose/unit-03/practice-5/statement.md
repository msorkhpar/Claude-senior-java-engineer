The page's last answer keeps an append-only log of commands and rebuilds
state by **replaying** them from the beginning (strictly, a log of
replayable commands is *command sourcing*). The log is also an audit trail:
you can ask for everything after a point in time.

`Account` (the receiver), and the records `Deposit(amount, timestamp)` and
`Withdraw(amount, timestamp)` are given; a `Withdraw` larger than the
balance throws `IllegalStateException`. Write `EventStore`:

- `new EventStore(account)` works on that live account.
- `append(command)` applies the command to the live account, then logs it.
  **Only a command that succeeded is logged**: if applying it throws, the
  exception reaches the caller and the log is unchanged.
- `replay()` returns a **new account, starting from zero**, with every
  logged command applied in log order. The live account is not touched.
- `eventsSince(t)` returns the logged commands whose timestamp is
  **strictly after** `t`, in log order.
- `events()` returns the whole log as a **snapshot**: appending later does
  not change a list already returned.

| appended | live balance | `replay().balance()` | `eventsSince(200)` |
|---|---|---|---|
| `Deposit(500, 100)`, `Withdraw(200, 200)`, `Deposit(1000, 300)` | 1300 | 1300 | `[Deposit(1000, 300)]` |
| then `Withdraw(5000, 400)` | throws, still 1300 | 1300 | unchanged |
