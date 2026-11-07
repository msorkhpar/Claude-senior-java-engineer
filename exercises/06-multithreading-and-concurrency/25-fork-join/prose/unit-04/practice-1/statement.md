At month end the bank adds interest to every account. The balances, in cents, sit in one `long[]`
of a few million entries, and `Accrual` adjusts them in place on a `ForkJoinPool`. The month-end
report reads the array the moment `accrue()` returns. On the test book of thirty accounts the
report is always right; on the real one, auditors find accounts with no interest and accounts that
got it twice.

Finish `compute()` of the `Adjust` task in `src/main/java/practice/Accrual.java`. `adjustHere()`
already adjusts a task's own range on the current thread. When you're done:

- a range of up to `threshold` balances is adjusted by its own task;
- a longer range is adjusted by several of the pool's workers at once;
- on a book of any size, every balance gets exactly one month of interest;
- when `accrue()` returns, the report sees every balance adjusted.

About seven lines in total. The tests run `Accrual` on real pools of two and three workers, and
one test holds a single account's adjustment for a while.
