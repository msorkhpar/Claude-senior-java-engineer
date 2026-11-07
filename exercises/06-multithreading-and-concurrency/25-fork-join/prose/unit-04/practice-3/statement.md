This practice continues the interest accrual: `Accrual.java` here is the finished version of the
previous practice, with `Adjust` working as you wrote it.

Last month one account held a corrupt negative balance, the adjustment threw on it, and the
accrual failed. The operator fixed the account and ran the accrual again, and thousands of
customers were paid two months of interest. The bank wants the accrual to be all or nothing.

Rewrite `accrue()` in `src/main/java/practice/Accrual.java`. When you're done:

- a book with no bad account is adjusted as before;
- when any adjustment fails, `accrue()` fails with that exception, and the array the caller passed
  holds exactly what it held before the call, then and afterwards.

About four lines in total. One test holds part of the accrual until after the failure has reached
the caller.
