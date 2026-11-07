# 8.3.3. Benefits of DRY for Concurrency

Each code of the spring campaign may be redeemed three times. A week after launch, finance finds
codes redeemed four and five times. The web checkout reads how often a code was used and, if
that is under the limit, adds one, so two orders that read at the same moment both pass. The team
makes the web checkout check and update in one atomic step, its load test goes from oversold
to clean, and the fix ships. The next week's report still shows oversold codes.

## A Fix Made in One Copy of a Check-Then-Update Leaves the Other Copy Racing

The counters live in `PromoCodes`, which hands each checkout a code's counter:

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** How many times each promo code has been redeemed, and how many times it may be. */
public class PromoCodes {
    private final int limit;
    private final Map<String, AtomicInteger> used = new ConcurrentHashMap<>();

    public PromoCodes(int limit) {
        this.limit = limit;
    }

    public int limit() {
        return limit;
    }

    public AtomicInteger counter(String code) {
        return used.computeIfAbsent(code, c -> new AtomicInteger());
    }
}
```

The mobile checkout, which nobody looked at during the incident, has the same lines the web
checkout had:

```java
import java.util.concurrent.atomic.AtomicInteger;

public class MobileCheckout {
    private final PromoCodes codes;

    public MobileCheckout(PromoCodes codes) {
        this.codes = codes;
    }

    /** True if the code still had a redemption left and this order took it. */
    public boolean apply(String code) {
        AtomicInteger used = codes.counter(code);
        if (used.get() >= codes.limit()) return false;   // validate
        used.incrementAndGet();                           // update
        return true;
    }
}
```

After its fix, the web checkout uses `getAndUpdate`, which stores the function's result only if no
other thread changed the count in between, and retries otherwise. The function adds one only while
the count is under the limit:

```java
public class WebCheckout {
    private final PromoCodes codes;

    public WebCheckout(PromoCodes codes) {
        this.codes = codes;
    }

    /** True if the code still had a redemption left and this order took it. */
    public boolean apply(String code) {
        int limit = codes.limit();
        int before = codes.counter(code).getAndUpdate(n -> n < limit ? n + 1 : n);
        return before < limit;                            // validate and update in one step
    }
}
```

The load test releases eight threads together; each tries each of 20,000 codes once, in the same
order; the mixed rows split the eight threads four and four. A code is oversold when more than
three of its eight attempts succeed. Each row is 20 runs on fresh counters:

```text
8 threads, 20000 codes, limit 3, 20 runs each
web, before its fix          20 of 20 runs oversold, worst run: 2905 codes
web, after its fix            0 of 20 runs oversold, worst run: 0 codes
mobile                       20 of 20 runs oversold, worst run: 3825 codes
web (fixed) and mobile       20 of 20 runs oversold, worst run: 2109 codes
```

The first two rows are the team's load test, which the fix passed. The mobile row is the same
check-then-update, still racing. The last row is production: both checkouts share the codes, and
the fixed web copy doesn't protect them. The mobile copy reads 2, a web order moves the counter to
3, and the mobile copy adds a fourth. This is the check-then-act of unit 6.2.1: each call is
atomic, and the pair is not. On one thread nothing can come between the mobile copy's check and
its update, so it refuses the fourth redemption too; only a test that runs several threads through
the unfixed copy finds the race. With the incident closed, the next report's oversold codes look
like a new bug.

## A Fix Made in the Owning Class Reaches Every Caller Without Touching Them

Unit 8.3.2 shows how to move a compound action into the class that guards its state. Here
`PromoCodes.tryRedeem` first held the mobile checkout's `get` and `incrementAndGet`, racing as
before; after the fix it reads:

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** How many times each promo code has been redeemed, and how many times it may be. */
public class PromoCodes {
    private final int limit;
    private final Map<String, AtomicInteger> used = new ConcurrentHashMap<>();

    public PromoCodes(int limit) {
        this.limit = limit;
    }

    /** True if the code still had a redemption left and the caller took it. */
    public boolean tryRedeem(String code) {
        AtomicInteger counter = used.computeIfAbsent(code, c -> new AtomicInteger());
        return counter.getAndUpdate(n -> n < limit ? n + 1 : n) < limit;
    }
}
```

The checkouts never see a counter. Each is now one line, and `MobileCheckout` is the same class
under another name:

```java
public class WebCheckout {
    private final PromoCodes codes;

    public WebCheckout(PromoCodes codes) {
        this.codes = codes;
    }

    public boolean apply(String code) {
        return codes.tryRedeem(code);
    }
}
```

The same load test, before and after the one edit to `tryRedeem`:

```text
owner before its fix: 8 threads, 20000 codes, limit 3, 20 runs each
web                          20 of 20 runs oversold, worst run: 2524 codes
mobile                       19 of 20 runs oversold, worst run: 2489 codes
web and mobile               20 of 20 runs oversold, worst run: 2679 codes
owner after its fix: 8 threads, 20000 codes, limit 3, 20 runs each
web                           0 of 20 runs oversold, worst run: 0 codes
mobile                        0 of 20 runs oversold, worst run: 0 codes
web and mobile                0 of 20 runs oversold, worst run: 0 codes
```

Every row went to zero, mixed traffic included. javac produced the same bytes for
`WebCheckout.class` and `MobileCheckout.class` before and after the fix, so a load test of
`tryRedeem` covers every caller that only delegates to it.

> **One machine.** Java 21, 20 cores, each program run three times: every racing row was oversold
> in 19 or 20 of its 20 runs, every fixed row in none.

`getAndUpdate` may call its function more than once when threads collide (unit 5.3.2), so the
function only computes the new count; anything with a side effect, such as recording the order,
goes after `tryRedeem` returns `true`.

<!-- practice 1 -->

## The Fix Holds Only While No Caller Can Reach the Counter

A month later, a support tool needs to apply codes to phone orders, and its author pastes the
redemption lines from an old branch of the mobile checkout:

```java
import java.util.concurrent.atomic.AtomicInteger;

/** Lets an agent apply a code to a phone order: the old mobile lines, pasted in. */
public class SupportDesk {
    private final PromoCodes codes;

    public SupportDesk(PromoCodes codes) {
        this.codes = codes;
    }

    public boolean apply(String code) {
        AtomicInteger used = codes.counter(code);
        if (used.get() >= codes.limit()) return false;
        used.incrementAndGet();
        return true;
    }
}
```

Against the fixed `PromoCodes`:

```text
pasted/SupportDesk.java:12: error: cannot find symbol
        AtomicInteger used = codes.counter(code);
                                  ^
  symbol:   method counter(String)
  location: variable codes of type PromoCodes
pasted/SupportDesk.java:13: error: cannot find symbol
        if (used.get() >= codes.limit()) return false;
                               ^
  symbol:   method limit()
  location: variable codes of type PromoCodes
2 errors
```

The public `counter()` of the first design is what made the copies possible: any class holding the
`AtomicInteger` could write its own check-then-update, and nothing but review would stop it. With
`counter()` gone, and `limit()`, which only the copies needed, the third copy fails to compile,
and the author has to call `tryRedeem`.

Not every accessor reopens the race. A method `int redeemed(String code)` that returns the count
as a number is safe to add: a caller may check it, but the only way to change the count is
`tryRedeem`, which checks again.

<!-- practice 2 -->

## Summary

- **A fix made in one copy of a check-then-update leaves every other copy racing.** The fixed
  copy's own load test passes, and with mixed traffic the codes are still oversold.
- **A fix made in the owning class reaches every caller without an edit to them.** One method and
  one load test cover every path; the callers compiled to the same bytes.
- **The fix holds only while no caller can reach the state around the owner's method.** Return
  values, not the counter, and a pasted copy stops compiling.

## Reference

**One call that validates and updates (Java 21 javadoc):**

| Need | Call | Caveat in the javadoc |
|---|---|---|
| a counter with a ceiling | `AtomicInteger.getAndUpdate(n -> n < limit ? n + 1 : n)` | the function should be side-effect-free; it may be re-applied under contention |
| a per-key value in a map | `ConcurrentHashMap.compute(key, (k, v) -> ...)` | the whole call is atomic and the function runs once; keep it short and simple, and don't modify the map from it |
| create a value once per key | `ConcurrentHashMap.computeIfAbsent(key, k -> ...)` | atomic; the function runs at most once, only if the key is absent; keep it short and simple, and don't modify the map from it |

## Challenges

1. The redemption limit moves into a database that three services in different languages share.
   Where is the owner now, and what is the single step that validates and updates?
2. Finance adds a per-customer limit next to the per-code one. Can `tryRedeem` still be one atomic
   step, and what does keeping two counters consistent cost?
3. A code base has a dozen copies of a check-then-update against a shared cache. Which change
   tells you, with a compile, that you have found all of them?

## Design Note: Why the Counter Is Hidden Rather Than Documented

The first design had a rule, "check and update a code's count in one step", that nothing in the
code enforced. Hiding the counter moves the rule into the type: the only operation a caller has is
the one that is correct.

The cost is that the owner must offer every compound action callers need. A checkout that wants
"redeem these two codes together, or neither" can't build it from two `tryRedeem` calls; the owner
needs a new method, and with it a lock or a single holder for both counts. Each such method makes
the owner larger, and merging actions that only look alike has its own failure, which unit 8.3.4
shows.

## Code Examples

- Test: [DryBenefitsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBenefitsTest.java)
- Source: [DryBenefits.java](src/main/java/com/github/msorkhpar/claudejavatutor/dryprinciple/DryBenefits.java)
