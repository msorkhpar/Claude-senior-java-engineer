# 4.1.4. Best Practices and Readability Considerations

The nightly invoice job fails, and the top frame of our code in the alert is
`at Invoices.lambda$totalInline$0(Invoices.java:11)`. The change passed review: every lambda in it
looked short. Each section below is a defect a reviewer can catch in a lambda. Outputs are from
Java 21.

## A Long Lambda Is a Method Without a Name, and Its Stack Frame Shows It

Two versions of the invoice total, run on a line whose `discount` is `null` (the program prints
the first three frames):

```java
.map(line -> {
    BigDecimal gross = line.price().multiply(BigDecimal.valueOf(line.qty()));
    BigDecimal off = gross.multiply(line.discount());
    return gross.subtract(off);
})
```

```java
.map(Invoices::netAmount)          // the same three lines, moved into static BigDecimal netAmount(Line line)
```

```text
inline: java.lang.NullPointerException: Cannot read field "scale" because "multiplicand" is null
	at java.base/java.math.BigDecimal.multiply(BigDecimal.java:1575)
	at Invoices.lambda$totalInline$0(Invoices.java:11)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	... 10 more
named: java.lang.NullPointerException: Cannot read field "scale" because "multiplicand" is null
	at java.base/java.math.BigDecimal.multiply(BigDecimal.java:1575)
	at Invoices.netAmount(Invoices.java:25)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	... 10 more
```

javac compiles a lambda body into a private method named after the enclosing method and a
counter (unit 4.1.1). The trace gets that name and a line number, and neither says which rule
failed. `netAmount` does, without opening the source at the right revision, and a test can call
it on one `Line` without building a stream.

Once a lambda has a block body with its own local variables, treat it as a method: name it and
pass it by method reference (units 4.3.x). A lambda whose whole meaning fits on the call site
(`line -> line.qty() > 0`) stays inline.

<!-- practice 5 -->

## The Method You Pass a Lambda To Decides When, How Often, and Whether It Runs

An account lookup falls back to creating an account. We look up three customers who all exist:

```java
Optional.ofNullable(accounts.get(c)).orElse(createAccount(c));
Optional.ofNullable(accounts.get(c)).orElseGet(() -> createAccount(c));
```

```text
orElse:     accounts created for 3 existing customers: 3
orElseGet:  accounts created for 3 existing customers: 0
```

`orElse` takes a value, so Java evaluates `createAccount(c)` before the call, every time; in the
service that was an INSERT per lookup. `orElseGet` takes a `Supplier`, and `Optional` calls it only
when it is empty. Logging has the same shape: `java.util.logging.Logger.fine(String)` gets a
message built whether or not `FINE` is on, and `fine(Supplier<String>)` builds it only if it is:

```text
fine(String):   carts dumped, FINE off: 1000
fine(Supplier): carts dumped, FINE off: 0
```

The receiving method's javadoc says when the lambda runs and what it may do while it runs.

<!-- practice 1 -->

### Spot the Problem: a Price Memo That Fails Only When a Bundle's Parts Are Unpriced

`prices` is a `HashMap` memo, `parts` maps a bundle to its SKUs, and `base` holds unit prices:

```java
static int price(String sku) {
    return prices.computeIfAbsent(sku, s -> parts.containsKey(s)
            ? parts.get(s).stream().mapToInt(Lazy::price).sum()
            : base.get(s));
}
```

`price("cable")` returns 9. Then `price("kit")`, a cable plus a charger:

```text
price(kit): java.util.ConcurrentModificationException
	at java.base/java.util.HashMap.computeIfAbsent(HashMap.java:1229)
	at Lazy.price(Lazy.java:31)
prices now: {cable=9, charger=25}
```

Pricing the charger inserted it into the same map while `computeIfAbsent` was running. `Map`'s
javadoc says the mapping function should not modify the map, and since Java 9 `HashMap` throws
when it detects it, on a best-effort basis. The memo passes any test whose parts were priced first, because then nothing
is inserted. Compute the value outside the map, then `put` it.

<!-- practice 2 -->

## State a Lambda Changes Through an Array or a Field Is Shared by Every Invocation

A lambda can't assign a captured local (unit 4.1.1), so `count++` doesn't compile and a
one-element array does. A quarter of a million rows out of a million are rejected:

```java
int[] count = {0};
IntStream.range(0, rows).parallel().forEach(row -> {
    if (rejected(row)) count[0]++;
});
long counted = IntStream.range(0, rows).parallel().filter(Counter::rejected).count();
```

```text
run 1: int[] counter 75,061, count() 250,000
run 2: int[] counter 63,294, count() 250,000
run 3: int[] counter 29,714, count() 250,000
```

The array reference is final; its contents are not. Every invocation of the lambda writes the same
slot, and a parallel stream invokes it from several threads with no synchronisation, so increments
are lost (20 cores; the loss varies by run). A field is shared by every call of the method too:
counts add up across runs, or one caller resets another's. Let the
library count (`count()`, a collector, units 4.4.x), or keep the state local to one call on one
thread.

<!-- practice 3 -->

## A Lambda Keeps Its Object Alive Only When It Uses `this` or an Instance Member

A 50 MB report screen registers a listener in a static list, then closes. One listener reads the
screen's `topic` field, one calls its `label()` method, and one, in a third method, uses a local
copy of `topic`:

```java
LISTENERS.add(msg -> System.out.println(topic + ": " + msg));      // means this.topic
LISTENERS.add(msg -> System.out.println(label() + ": " + msg));    // means this.label()
String t = topic;
LISTENERS.add(msg -> System.out.println(t + ": " + msg));
```

We drop the screen, call `System.gc()`, and print whether a `WeakReference` to it was cleared and
the types of the listener object's fields:

```text
field  screen collected: false  captured fields: ReportScreen 
method screen collected: false  captured fields: ReportScreen 
local  screen collected: true   captured fields: String 
```

A lambda captures what its body uses (unit 4.1.1). An instance field or method used without a qualifier is used
through `this`, so the lambda holds the whole screen, and the static list keeps 50 MB reachable
for every screen that closed. A lambda that uses only locals and parameters holds no `this`.
`System.gc()` is only a request, so a test of this asks more than once.

<!-- practice 4 -->

## Summary

- **A long lambda is a method without a name.** Its frame reads `lambda$method$0`; a named method
  tells the trace and the test which rule ran.
- **The receiver decides when, how often and whether a lambda runs.** `orElse` evaluates its
  argument every time; a mapping function that inserts into its own `HashMap` throws.
- **State changed through an array or a field is shared by every invocation.** On several threads,
  increments are lost; a field also carries over between calls.
- **A lambda holds `this` only when it uses `this` or an instance member.** Registered somewhere long-lived,
  it keeps its whole object alive.

## Reference

| Argument evaluated at every call | Lambda run only when needed |
|---|---|
| `Optional.orElse(T)` | `Optional.orElseGet(Supplier)` |
| `Objects.requireNonNullElse(T, T)` | `Objects.requireNonNullElseGet(T, Supplier)` |
| `Logger.fine(String)`, and each other level | `Logger.fine(Supplier<String>)`, and each other level |

## Challenges

1. Your team's rule is "lambdas at most three lines". Write a two-line lambda that should still be
   a named method, and a five-line one that shouldn't.
2. A `ConcurrentHashMap` memo prices a bundle from its parts. Read its `computeIfAbsent` javadoc:
   what may the mapping function do, and how would you structure the memo?
3. A listener has to call an instance method of its owner. How do you register it without keeping
   the owner alive, and who unregisters it?

## Design Note: Why a Lambda's Frame Has No Useful Name

Java gives a lambda no name of its own, which suits code short enough that the call site explains
it. The method javac generates exists only to hold the body, so its name records where the body
was, not what it means. The cost falls on whoever reads traces and heap dumps later, and catching
a long lambda in review costs less than reading its trace in production.

## Code Examples

- Test: [LambdaBestPracticesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaBestPracticesTest.java)
- Source: [LambdaBestPractices.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaBestPractices.java)
