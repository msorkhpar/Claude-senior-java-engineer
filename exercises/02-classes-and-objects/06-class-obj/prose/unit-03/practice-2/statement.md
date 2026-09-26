An **inner class** (a nested class that is not `static`) is an instance member:
each of its objects belongs to one object of the outer class and can read that
object's instance fields directly. Since Java 16 an inner class may also declare
**static** members, which belong to the inner class as a whole.

Write `Bank` and its inner class `Bank.Account`:

- `Bank(double ratePercent)`, `setRate(double ratePercent)`;
- `Account open(double balance)` opens an account **at this bank**;
- `Account.balance()` returns the balance;
- `Account.yearlyInterest()` returns `balance * rate / 100`, using its own bank's
  rate **as it is now**;
- `static int Account.opened()` counts every account opened so far, at any bank.

**Examples**

```
b = new Bank(2.0); a = b.open(1000)
a.yearlyInterest()       -> 20.0
b.setRate(3.0)
a.yearlyInterest()       -> 30.0
Bank.Account.opened()    -> grows by 1 for each open
```
