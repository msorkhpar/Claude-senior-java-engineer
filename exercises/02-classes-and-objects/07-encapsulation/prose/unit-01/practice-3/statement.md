The page's edge case: **a nested class can use the private members of the class
that encloses it.** `Wallet` keeps its owner and balance in private fields and has a
private constructor. Two nested classes work with those private members directly:

- `Wallet.Builder` (a static nested class) collects `owner(String)` and
  `cents(long)`, and `build()` returns a new wallet made with the **private**
  constructor. Wallet must not gain a public constructor.
- `Wallet.Auditor` (an inner class, one per wallet, from `wallet.auditor()`)
  - `report()` returns `"<owner>: <cents>"`, read from the wallet **at the moment
    it is called**;
  - `correct(long delta)` adds `delta` (which may be negative) to the wallet's
    private balance.

`deposit(long cents)` and `balance()` work on the wallet as usual.

Examples:

```
Wallet w = Wallet.builder().owner("Ana").cents(250).build();
Wallet.Auditor a = w.auditor();
a.report()            -> "Ana: 250"
w.deposit(100);
a.report()            -> "Ana: 350"
a.correct(-50);
w.balance()           -> 300
```
