# 9.3.1. Strategy Pattern

The checkout service prices each cart through a pricing policy whose name comes from config:
`pricing.policy=bulk`. In one month it had two incidents. After the request thread pool was enlarged,
a few customers a day were charged a total mixed from their cart and someone else's. Then a deploy
with `pricing.policy=Bulk` started, passed its health check, and failed at the first checkout 40
minutes later. Neither change has a line that looks wrong in review.

A strategy is an object the caller hands in to do one step of its work, so the caller doesn't
decide how that step is done. All output is from Java 21.

## A Lambda Strategy Has No Name; an Enum Strategy Has One, and One Instance per JVM

The interface has one method, so any lambda from a cart to a `long` is a policy. The same
bulk rule, 10 % off a cart of ten units or more, written once as a lambda and once as an enum
constant:

```java
import java.util.List;

public class Pricing {
    record Line(String sku, int qty, long unitCents) {}

    @FunctionalInterface
    interface PricingPolicy {
        long totalCents(List<Line> cart);
    }

    enum Policy implements PricingPolicy {
        STANDARD {
            public long totalCents(List<Line> cart) {
                return cart.stream().mapToLong(l -> l.qty() * l.unitCents()).sum();
            }
        },
        BULK {                                    // 10 % off a cart of 10 units or more
            public long totalCents(List<Line> cart) {
                long sum = STANDARD.totalCents(cart);
                int units = cart.stream().mapToInt(Line::qty).sum();
                return units >= 10 ? sum * 90 / 100 : sum;
            }
        }
    }

    public static void main(String[] args) {
        PricingPolicy bulkLambda = cart -> {
            long sum = Policy.STANDARD.totalCents(cart);
            int units = cart.stream().mapToInt(Line::qty).sum();
            return units >= 10 ? sum * 90 / 100 : sum;
        };
        List<Line> cart = List.of(new Line("mug", 12, 450));
        for (PricingPolicy p : List.of(bulkLambda, Policy.BULK)) {
            System.out.println("pricing with " + p + ": " + p.totalCents(cart));
        }
        System.out.println(Policy.valueOf("BULK") + " " + java.util.Arrays.toString(Policy.values()));
    }
}
```

```text
pricing with Pricing$$Lambda/0x00007126dc000c08@330bedb4: 4860
pricing with BULK: 4860
BULK [STANDARD, BULK]
```

Both price the cart the same. The lambda's `toString()` is a generated class name that can
change between runs, so a log line, a metric tag or an incident timeline can't say which policy
priced a cart, and turning a config string into a lambda needs a table you write. The enum
constant prints its name, `valueOf` turns a config string into the constant, and `values()`
lists every choice for an error message.

Its set is closed: no code outside the enum can add a constant.
A constant can't take parameters per use, so a 12 % bulk rate for one tenant and 8 % for
another needs a class or a lambda that captures the rate. And each constant is a single object
per class loader, which in practice means one for the whole JVM. Use the enum for a fixed set of
named policies that carry no settings, and a lambda or a small record for policies built from
configuration.

When the set of policies is fixed and the questions you ask about them keep growing, the
alternative is a sealed interface of records with one `switch` per question (unit 8.1.2).

<!-- practice 4 -->

## A Strategy Object Is Shared, So a Field in It Is Shared State

One policy object prices every cart: an enum constant, a `static final` lambda or a singleton
bean is one instance serving every request thread. The bulk policy was refactored into helper
steps that pass their running totals through two fields:

```java
static final class BulkWithFields implements PricingPolicy {
    private long sum;
    private int units;

    public long totalCents(List<Line> cart) {
        sum = 0;
        units = 0;
        for (Line l : cart) add(l);
        return discounted();
    }

    private void add(Line l) {
        sum += l.qty() * l.unitCents();
        units += l.qty();
    }

    private long discounted() {
        return units >= 10 ? sum * 90 / 100 : sum;
    }
}
```

Every unit test passes, because a test prices one cart at a time. Eight threads now price
200,000 carts each through one instance, and we count totals that differ from a single-threaded
calculation: for this class, for the same class with both fields `volatile`, and for this version,
which keeps the running totals in a value each call owns:

```java
static final class BulkStateless implements PricingPolicy {
    private record Tally(long sum, int units) {
        Tally add(Line l) { return new Tally(sum + l.qty() * l.unitCents(), units + l.qty()); }
        long discounted() { return units >= 10 ? sum * 90 / 100 : sum; }
    }

    public long totalCents(List<Line> cart) {
        Tally t = new Tally(0, 0);
        for (Line l : cart) t = t.add(l);
        return t.discounted();
    }
}
```

```text
BulkWithFields           10,078 of 1,600,000 carts priced wrong
BulkWithVolatileFields   1,521,082 of 1,600,000 carts priced wrong
BulkStateless            0 of 1,600,000 carts priced wrong
```

One thread resets `sum` while another is halfway through its cart, and the second thread's
discount is computed on a mix of both carts: between one cart in 140 and one in 175 over four
runs. `volatile` made it far worse. It forces every read and write of `sum` and `units` to
memory, so each `+=` picks up the other threads' partial totals, and it doesn't make `+=`
atomic. The plain fields probably went wrong less
often because the JIT kept them in registers for part of each call, which nothing promises.
`synchronized` on `totalCents` would be correct, and every checkout in the service would then
queue for one lock.

The policy needs nothing from one call to the next, so it should hold no fields. A strategy
that does need state across calls, such as a round-robin counter, holds it in an atomic type
(unit 5.3.1) and says so in its javadoc.

<!-- practice 1 -->

## The JDK's Comparator Is a Strategy, and the Sort Trusts Its Contract

`List.sort`, `TreeMap`, `PriorityQueue` and `Collections.max` take a `Comparator` and don't
validate it. The order history sorts orders by creation time, a `long` of epoch milliseconds,
with a comparator that looks equivalent to `comparingLong`:

```java
Comparator<Order> subtracting = (a, b) -> (int) (a.createdMillis() - b.createdMillis());
Comparator<Order> comparing = Comparator.comparingLong(Order::createdMillis);
```

First three orders created on day 0 (`A-1`), day 20 (`A-2`) and day 30 (`A-3`), in two input
orders; then five random sets of 1,000 orders spread over a year, counting neighbours left out
of order:

```text
subtracting:   [A-3, A-2, A-1] -> [A-1, A-2, A-3]
comparingLong: [A-3, A-2, A-1] -> [A-1, A-2, A-3]
subtracting:   [A-1, A-3, A-2] -> [A-2, A-3, A-1]
comparingLong: [A-1, A-3, A-2] -> [A-1, A-2, A-3]
subtracting:   441 425 427 433 java.lang.IllegalArgumentException: Comparison method violates its general contract!
comparingLong: 0 0 0 0 0
```

Thirty days are 2,592,000,000 ms, which doesn't fit an `int`. The cast keeps the low 32 bits
and flips the sign, so the comparator reports day 30 as earlier than day 0, and whether the
sort ever asks about that pair depends on the input order. Tests with one day's orders never get 24.8 days
apart, and pass. On a year of orders the comparator says a < b, b < c and c < a for some
triples, and `List.sort` assumes that never happens. Four times it returned a scrambled list
without a word; once its merge step ran into the contradiction and threw. Which of the two you
get depends on the input, so one customer's history page throws and another's is silently
misordered.

`Long.compare`, `Integer.compare` and the `comparingLong`/`comparingInt` factories can't
overflow. Unit 4.1.3 covers `reversed()` on a comparator chain, and unit 1.4.4 a `TreeSet` that
treats a 0 from its comparator as "duplicate": there too, the collection acts on whatever the
comparator answers.

<!-- practice 2 -->

## Resolve a Strategy's Name When the Service Starts, Not When It First Runs

The policies sit in a `Map<String, PricingPolicy>`, and config names one. Three versions of the
checkout service are each started with `pricing.policy=Bulk`. The first looks the name up on
each checkout:

```java
long checkout(List<Line> cart) {
    PricingPolicy policy = POLICIES.get(policyName);
    return policy.totalCents(cart);
}
```

The second resolves it in the constructor with `POLICIES.getOrDefault(name, STANDARD)`, where
`STANDARD` is the standard policy as a lambda. The
third resolves it in the constructor and refuses a name it doesn't know:

```java
Checkout(Map<String, String> config) {
    String name = config.getOrDefault("pricing.policy", "standard");
    policy = POLICIES.get(name);
    if (policy == null) {
        throw new IllegalArgumentException("pricing.policy=" + name + " is not one of "
                + new TreeSet<>(POLICIES.keySet()));
    }
}
```

```text
lazy:     started; first checkout: java.lang.NullPointerException: Cannot invoke "Config$PricingPolicy.totalCents(java.util.List)" because "policy" is null
fallback: started; first checkout: 5400
eager:    start-up failed: java.lang.IllegalArgumentException: pricing.policy=Bulk is not one of [bulk, standard]
```

The lazy service passed its health check, so the deploy reached every node, and the typo
surfaced at the first checkout as an exception that names neither the key nor the value. The
fallback is worse: it never fails, and every bulk order is charged the standard 5400 instead of
4860 until finance notices. The eager service refuses to start, the deploy stops on the first
node, and the message tells the operator what to type.

A default belongs to a key that is absent, never to a value that is wrong, so `getOrDefault`
belongs on `config`, never on `POLICIES`. An enum's `valueOf("Bulk")` called in the constructor
fails the same way, with `No enum constant ValueOf.Policy.Bulk` (`ValueOf` is the demo class),
which lists none of the names that would work.

<!-- practice 3 -->

## Summary

- **A lambda strategy has no name; an enum constant has one.** The enum gives `valueOf` and
  `values()` for config and logs, and gives up parameters, extension by other modules, and
  separate instances.
- **A strategy object is shared, so its fields are shared state.** Keep per-call values in
  locals or a value the call owns; `volatile` makes the race more frequent, and `synchronized`
  makes every caller queue.
- **A caller trusts its strategy's contract.** A comparator that subtracts overflows, and
  `List.sort` returns a wrong order or throws, depending on the input.
- **Resolve a strategy name at start-up.** A lookup at first use fails after the deploy, and a
  fallback for an unknown name never fails at all.

## Reference

| Form of the strategy | Name in logs | From a config string | Per-use parameters |
|---|---|---|---|
| lambda or method reference | generated class name | a `Map` you write | captured variables |
| enum constant | `toString()`, which defaults to `name()` | `valueOf`, throws `IllegalArgumentException` | none: one instance per constant |
| record | generated from its components: `Bulk[percent=12]` | a `Map` you write | constructor arguments |
| class | the `toString()` you write | a `Map` you write | constructor arguments |

| `Comparator` written as | Can overflow |
|---|---|
| `(a, b) -> a - b` on `int`, `(int) (a - b)` on `long` | yes, when the difference doesn't fit an `int` |
| `Integer.compare`, `Long.compare` | no |
| `Comparator.comparingInt`, `comparingLong` | no |

## Challenges

1. A partner team wants to plug its own pricing policy into your checkout. Which form in the
   reference table lets them, and what do you lose in your logs?
2. Your round-robin load balancer is a strategy with an `int next` field. Make it correct under
   threads, then decide what happens when the counter passes `Integer.MAX_VALUE`.
3. The pricing policy needs a tax rate from a remote service. Where does the rate live so that
   the policy stays stateless, and who decides when it is refreshed?
4. Find a strategy chosen by name in code you work on. What happens today when the name is
   misspelled, and when does anyone find out?

## Design Note: What the Interface Can't Say

Strategy buys pluggability: the checkout prices carts with policies written after it, and the
sort orders types it has never seen. The price is that the caller's assumptions about the
strategy live outside the type system. `PricingPolicy` and `Comparator` each declare one method
signature, and nothing in a signature says "safe to call from many threads at once" or "a
consistent total order". javac accepts a policy with fields and a comparator that overflows,
and the caller can't check either at run time. Those assumptions go in the interface's javadoc,
and in tests that call the strategy the way the caller does: from many threads, and with
values far apart.

## Code Examples

- Test: [StrategyPatternTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/StrategyPatternTest.java)
- Source: [StrategyPattern.java](src/main/java/com/github/msorkhpar/claudejavatutor/behavioralpatterns/StrategyPattern.java)
