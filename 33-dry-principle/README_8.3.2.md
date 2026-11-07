# 8.3.2. Applying DRY in Concurrent Programming

A customer's prepaid wallet holds 100 credits. She buys an 80-credit item, her monthly plan renews
for 30, and afterwards her balance reads 70. Checkout, gifts and renewals each charge the wallet,
and each passed review. Checkout and gifts lock the wallet around the charge; the renewal job,
written last, doesn't.

"Hold the wallet's lock from the read to the write" is knowledge in the sense of unit 8.3.1, and
here every call site has to repeat it. Every output below is from Java 21 on a 20-core machine.

## A Lock Written at Each Call Site Is Missing at the Site That Forgets It

The wallet offers a read and a write, each `synchronized`, and leaves the charge to its callers:

```java
/** A customer's prepaid credits. A caller that reads and then writes holds the wallet's monitor across both. */
public class Wallet {
    private long credits;

    public Wallet(long credits) {
        this.credits = credits;
    }

    public synchronized long balance() {
        return credits;
    }

    public synchronized void setBalance(long credits) {
        this.credits = credits;
    }
}
```

Checkout follows the class comment, and `Gifts.send` has the same body. The renewal job asks the
tax service for the tax between its read and its write:

```java
public boolean buy(Wallet wallet, long price) {
    synchronized (wallet) {
        long balance = wallet.balance();
        if (balance < price) return false;
        wallet.setBalance(balance - price);
        return true;
    }
}

public boolean renew(Wallet wallet, long planPrice) throws InterruptedException {
    long balance = wallet.balance();
    long due = planPrice + taxes.on(planPrice);
    if (balance < due) return false;
    wallet.setBalance(balance - due);
    return true;
}
```

To get the bad order on every run, the test's tax service waits on a latch, and a checkout runs
while the renewal waits:

```java
CallSites.Taxes taxes = price -> {
    inTaxCall.countDown();
    taxAnswers.await();
    return 3;
};
// another thread runs renewals.renew(wallet, 27)
inTaxCall.await();
boolean bought = new CallSites.Checkout().buy(wallet, 80);
taxAnswers.countDown();
```

```text
renewal waits for the tax service; balance 100
checkout 80: true, balance 20
renewal 27 + 3 tax: true, balance 70
sold 110 credits, the balance fell by 30
```

The renewal kept 100 in a local variable, and its `setBalance(70)` overwrote the checkout's
charge. Each method of `Wallet` is atomic; the charge is a check followed by an act across two of
them (unit 6.2.1). The renewal takes the monitor for each call and lets go in between, so
checkout's lock, held from read to write, never keeps it out.

With an instant tax service and no latch, seven checkout threads and one renewal thread drain a
million-credit wallet, ten times:

```text
run  1: sold 1589562, debited 1000000, lost 589562
run  2: sold 1661016, debited 1000000, lost 661016
run  3: sold 1621688, debited 1000000, lost 621688
run  4: sold 1682094, debited 1000000, lost 682094
run  5: sold 1626598, debited 1000000, lost 626598
run  6: sold 1643494, debited 1000000, lost 643494
run  7: sold 1629660, debited 1000000, lost 629660
run  8: sold 1470406, debited 1000000, lost 470406
run  9: sold 1576134, debited 1000000, lost 576134
run 10: sold 1699756, debited 1000000, lost 699756
runs with lost credits: 10 of 10
```

Every run lost credits, as did 30 more, by different amounts each run. The losses are debits made
by the locked checkouts and overwritten by the renewal. The class's public setter is what lets
any caller compose the charge.

## The Compound Action Belongs in the Class That Owns the Lock

The charge moves into `Wallet`, and the setter goes:

```java
/** A customer's prepaid credits. Thread-safe: every read and change of credits holds this wallet's monitor. */
public class Wallet {
    private long credits;

    public Wallet(long credits) {
        this.credits = credits;
    }

    public synchronized long balance() {
        return credits;
    }

    public synchronized boolean tryDebit(long amount) {
        if (credits < amount) return false;
        credits -= amount;
        return true;
    }

    public synchronized void credit(long amount) {
        credits += amount;
    }
}
```

Checkout and gifts become `return wallet.tryDebit(price);`. The renewal asks for the tax first,
then calls `wallet.tryDebit(due)`. The same latch and the same order:

```text
renewal waits for the tax service; balance 100
checkout 80: true, balance 20
renewal 27 + 3 tax: false, balance 20
sold 80 credits, the balance fell by 80
```

The renewal still waits for the tax service at the same moment, but it holds no balance while it
waits. The ten stress runs end with `runs with lost credits: 0 of 10`, and so do three more sets
of ten. Clean runs are a sample; the reason it holds is in one file: after construction, every
method that reads or changes `credits` is `synchronized` on the wallet, with the lock named next
to the field as in unit 5.3.4.

Deleting `setBalance` also tells you where the old rule was written. Compiling the old call sites
against the new `Wallet`:

```text
CallSites.java:14: error: cannot find symbol
                wallet.setBalance(balance - price);
                      ^
  symbol:   method setBalance(long)
  location: variable wallet of type Wallet
CallSites.java:25: error: cannot find symbol
                from.setBalance(balance - amount);
                    ^
  symbol:   method setBalance(long)
  location: variable from of type Wallet
CallSites.java:42: error: cannot find symbol
            wallet.setBalance(balance - due);
                  ^
  symbol:   method setBalance(long)
  location: variable wallet of type Wallet
3 errors
```

javac lists all three call sites, the two locked ones and the renewal. A shared
`withLock(wallet, ...)` helper would remove the repeated `synchronized` lines but none of these
errors: each caller would still read, decide and write, and a caller that skips the helper still
compiles. What one fix in the owning class buys later is unit 8.3.3.

<!-- practice 1 -->

## An Action Across Two Wallets Needs One Owner for Both Balances

A gift is a debit from one wallet and a credit to another, two calls, and the reconciliation job
sums every wallet while gifts are in flight. Ann and Bo, with 100 credits each, send each other
one credit a million times while an auditor sums their balances, first with `Wallet` as it is:

```java
if (ann.tryDebit(1)) bo.credit(1);        // Ann sends Bo a gift
if (bo.tryDebit(1)) ann.credit(1);        // and Bo sends one back
// the auditor's total, read in a loop on another thread:
() -> ann.balance() + bo.balance()
```

then with one class that owns every balance:

```java
import java.util.HashMap;
import java.util.Map;

/** Every wallet's credits. Thread-safe: all of them are guarded by this object's monitor. */
public class Wallets {
    private final Map<String, Long> credits = new HashMap<>();

    public synchronized void open(String id, long amount) {
        credits.put(id, amount);
    }

    public synchronized boolean tryDebit(String id, long amount) {
        long balance = credits.get(id);
        if (balance < amount) return false;
        credits.put(id, balance - amount);
        return true;
    }

    public synchronized void credit(String id, long amount) {
        credits.merge(id, amount, Long::sum);
    }

    public synchronized boolean transfer(String from, String to, long amount) {
        if (!tryDebit(from, amount)) return false;
        credit(to, amount);
        return true;
    }

    public synchronized long total() {
        long sum = 0;
        for (long c : credits.values()) sum += c;
        return sum;
    }
}
```

```text
one lock per wallet  1,472,320 audits, 1,337,515 saw a total other than 200 (from 199 to 201)
one lock for all     2,644,166 audits, 0 saw a total other than 200 (from 200 to 200)
```

Two compound actions span both wallets: the gift, between its debit and its credit, and the audit,
between its two reads. Both 199 and 201 are audits whose two reads straddled part of a gift: one
that landed between a debit and its credit sees a credit missing, and one that read Ann before a
gift from her and Bo after it counts the credit twice. Each wallet's lock covers one balance,
and the rule "the total doesn't change" covers two, so its lock belongs to the object that owns
both.

The cost is that every charge, gift and audit in the process now waits for one monitor. Fixed-order
locking of both wallets (unit 6.2.1) keeps unrelated gifts parallel, but an audit must then take
every wallet's lock.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A lock written at each call site is missing at the site that forgets it.** One unlocked
  renewal wrote back a stale balance, overwriting debits made by the locked checkouts in every
  stress run.
- **The compound action belongs in the class that owns the lock.** With `tryDebit` the same forced
  order declines the renewal, and deleting the setter makes javac list every caller that composed
  the charge.
- **An action across two objects needs one owner for both.** Two thread-safe wallets still showed
  wrong totals to nine audits in ten; one class with one lock showed none, at the price of one
  monitor for all wallets.

## Reference

Where the JDK puts compound actions (Java 21 javadoc):

| Class | Compound actions the class performs atomically | Left to the caller |
|---|---|---|
| `Collections.synchronizedList` | none beyond single calls | iteration: "the user manually synchronize on the returned list when traversing it" |
| `ConcurrentHashMap` | `putIfAbsent`, `replace`, `compute`, `merge` for one key | actions across keys (unit 5.3.2) |
| `AtomicLong` | `compareAndSet`, `updateAndGet`, `accumulateAndGet` | actions across two variables |

## Challenges

1. Product asks for "pay what the wallet covers, the rest by card". Write it as a caller of
   `balance()` and `tryDebit()`, then as a method of `Wallet`. What can the caller version charge
   when a checkout runs in between?
2. `Wallets` serializes every wallet in the process. Measure checkouts per second with 1, 8 and 32
   threads, then try one lock per wallet with ordered locking for transfers. What does the audit
   cost in each design?
3. Find a class in your code whose Javadoc tells callers which lock to hold. Which of its callers
   would javac list if you removed the methods that make the rule necessary?

## Design Note: Why ConcurrentMap Has putIfAbsent

The synchronized collections made each call atomic and left compound actions to their callers,
with the collection itself as the lock; their javadoc still tells callers to lock it for iteration.
Java 5's `ConcurrentMap` moved the common compound actions into the interface (`putIfAbsent`,
`replace`, `remove(key, value)`), and Java 8 added `compute` and `merge`, where the caller passes
the decision as a function, which `ConcurrentHashMap` runs atomically.

A class can offer only the compound actions its authors foresaw, so a new one means a new method.
Accepting a function instead runs the caller's code while the map holds a lock of its own, and
`ConcurrentHashMap`'s javadoc asks that the function be "short and simple" and not modify the map.

## Code Examples

- Test: [DryConcurrencyPatternsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryConcurrencyPatternsTest.java)
- Source: [DryConcurrencyPatterns.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryConcurrencyPatterns.java)
