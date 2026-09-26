The page's Q4 shows what Spring's `@Transactional` does: it wraps the bean in
a proxy that begins a transaction before the method, commits after it, and
rolls back on an exception. Build that proxy with `java.lang.reflect.Proxy`.

Given: the annotation `@Transactional` (kept at run time, placed on interface
methods) and `TxManager` (`begin()`, `commit()`, `rollback()`). Write
`Transactions.wrap(target, type, tx)`, returning a proxy of `type`:

- a method marked `@Transactional`: `tx.begin()`, then the call on `target`,
  then `tx.commit()`; its result is returned;
- **a failure rolls back and is never committed**: if the method throws,
  `tx.rollback()` runs and the caller gets the method's own exception;
- **only methods marked `@Transactional` get a transaction**; the others are
  delegated as they are.

| call (on an `OrderService` proxy) | events | result |
|---|---|---|
| `placeOrder("A-1")` (marked) | `begin`, `place A-1`, `commit` | `"placed A-1"` |
| `placeOrder("BAD")` (marked, throws) | `begin`, `rollback` | `IllegalStateException("card declined")` |
| `describe("A-1")` (not marked) | `describe A-1` | `"order A-1"` |
