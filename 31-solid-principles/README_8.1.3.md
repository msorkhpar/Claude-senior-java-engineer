# 8.1.3. Liskov Substitution Principle (LSP)

The catalog service has a helper with a green test suite:

```java
static List<String> normalizeTags(List<String> tags) {
    tags.replaceAll(t -> t.trim().toLowerCase());
    tags.sort(null);
    for (int i = tags.size() - 1; i > 0; i--) {
        if (tags.get(i).equals(tags.get(i - 1))) tags.remove(i);
    }
    return tags;
}
```

A cleanup in the import job replaces `new ArrayList<>(Arrays.asList(csv.split(",")))` with
`List.of(csv.split(","))`,
and the next import fails with `UnsupportedOperationException`. The method takes a `List`, and
`List.of` returns a `List`. The Liskov substitution principle says code written against a type
must keep working with every subtype of it. Which of the two broke it?

## javac Checks an Override's Signature; Only a Caller Run on Every Subtype Checks Its Contract

javac enforces the part of a contract it can read. An override that wants to report a disk error
through a method that declares none doesn't compile:

```java
import java.io.IOException;
import java.util.List;

interface Exporter {
    Object export(List<String> rows);
}

class FileExporter implements Exporter {
    @Override
    public String export(List<String> rows) throws IOException {   // wants to report a disk error
        throw new IOException("disk full");
    }
}
```

```text
Exporter.java:10: error: export(List<String>) in FileExporter cannot implement export(List<String>) in Exporter
    public String export(List<String> rows) throws IOException {   // wants to report a disk error
                  ^
  overridden method does not throw IOException
1 error
```

javac accepts the narrower return type, `String` for `Object`, and an `UncheckedIOException` in
place of the checked one (unit 2.1.4 shows what such an override does to its callers). Which inputs
a method accepts, what it returns for them and which unchecked exceptions it throws are written
only in the javadoc. The one executable check is a caller written against the supertype and run
on every implementation. The runs below use this helper:

```java
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** Runs one caller, written against a supertype, on every implementation and prints what each did. */
public final class Substitute {

    public static <T> void check(String caller, Map<String, Supplier<T>> impls, Function<T, ?> call) {
        System.out.println(caller);
        impls.forEach((name, make) -> {
            String outcome;
            try {
                outcome = String.valueOf(call.apply(make.get()));
            } catch (RuntimeException e) {
                outcome = e.getClass().getSimpleName();
            }
            System.out.printf("  %-30s %s%n", name, outcome);
        });
    }
}
```

Here it runs `normalizeTags` on `"web", " Java", "java", "api"`, built the five ways the
service's callers build a `List<String>`:

```text
normalizeTags
  new ArrayList<>(...)           [api, java, web]
  Arrays.asList(...)             UnsupportedOperationException
  List.of(...)                   UnsupportedOperationException
  unmodifiableList(...)          UnsupportedOperationException
  stream().toList()              UnsupportedOperationException
```

The test suite built its input with `new ArrayList<>`, the one row that works. `Arrays.asList` is
a fixed-size view of the array: it sorts in place and fails at `remove`. The others fail at the
first change.

## List's Mutators Are Optional Operations, So the Caller That Changes Its Argument Is in Breach

`List.add`, `remove` and `set` are each marked "(optional operation)" in the javadoc, and
`Collection` says what that means: an implementation that doesn't support one throws
`UnsupportedOperationException`. Throwing is part of `List`'s contract, so `List.of` keeps it.
`normalizeTags` is the code in breach: it relies on a promise `List` never made, so its signature
hides a precondition, "pass a modifiable list". The fix builds a new list and leaves the argument
alone:

```java
static List<String> normalizeTags(List<String> tags) {
    return tags.stream().map(t -> t.trim().toLowerCase()).distinct().sorted().toList();
}
```

```text
normalizeTags, fixed
  new ArrayList<>(...)           [api, java, web]
  Arrays.asList(...)             [api, java, web]
  List.of(...)                   [api, java, web]
  unmodifiableList(...)          [api, java, web]
  stream().toList()              [api, java, web]
```

Read methods have optional clauses too. `Collection.contains` may throw `NullPointerException` "if
the specified element is null and this collection does not permit null elements (optional)". A
gateway forwards a header if its name is on an allow-list, and a request arrives without the
header:

```java
static boolean forwarded(List<String> allowed, String header) {
    return allowed.contains(header);
}
```

```text
forwarded(allowed, null)
  new ArrayList<>(...)           false
  Arrays.asList(...)             false
  List.of(...)                   NullPointerException
  unmodifiableList(...)          false
  stream().toList()              false
```

Moving the allow-list from `Arrays.asList` to `List.of` passes every test that sends the header
and fails on the first request that doesn't. `Set.of` rejects a null query the same way.

The JDK made these subtypes legal by weakening the supertype: `List` promises so little that an
unmodifiable list keeps the contract. The cost moved to every caller, which must copy before
changing a list it was given and check for null before asking.

<!-- practice 1 -->

## Timestamp Breaks the Symmetry Object.equals Promises, So a Set of Dates Depends on Arrival Order

`Object.equals` makes a promise with no optional clause: `x.equals(y)` should be true exactly
when `y.equals(x)` is. `java.sql.Timestamp` extends `java.util.Date` and breaks it. An audit job counts
distinct event times; some come from `new Date()` in the service, others from
`rs.getTimestamp(...)`, held as `Date`:

```java
static int distinctTimes(List<Date> times) {
    return new HashSet<>(times).size();
}
```

```java
long millis = Instant.parse("2026-03-15T09:00:00.250Z").toEpochMilli();
Date fromApp = new Date(millis);              // new Date() in the service
Date fromDb = new Timestamp(millis);          // rs.getTimestamp("created_at")
```

```text
fromApp.equals(fromDb): true
fromDb.equals(fromApp): false
same hashCode:          true
distinctTimes, app first: 2, db first: 1
```

`Date.equals` compares milliseconds and accepts any `Date`. `Timestamp.equals` returns false for
anything that isn't a `Timestamp`, even at the same millisecond: its javadoc gives the reason that
a `Date`'s nanoseconds are unknown. A `HashSet` calls
`equals` on the element being added, and the same moment counts once or twice depending on which
source answered first. Timestamp's javadoc admits it: the inheritance "really denotes
implementation inheritance, and not type inheritance", and code should not treat a `Timestamp` as
a `Date`. The fix follows that advice and converts at the boundary, keying by `Instant`:

```java
static int distinctTimesFixed(List<Date> times) {
    Set<Instant> seen = new HashSet<>();
    for (Date d : times) seen.add(d.toInstant());
    return seen.size();
}
```

```text
fixed,         app first: 1, db first: 1
```

The same reuse through `extends` gave `java.sql.Date` overrides that throw (unit 7.3.1) and
`Properties` an inherited `put` its own methods can't read back (unit 2.3.1).

<!-- practice 2 -->

## A Subtype May Accept More and Promise More, Never Less

In your own hierarchy, the supertype's javadoc is the contract, and the caller test is written
against the interface, next to that javadoc. Checkout takes any `Discount`:

```java
/** A discount on a cart. */
interface Discount {
    /**
     * @param subtotalCents the cart's subtotal: any value from 0 (an empty cart) up
     * @return the discount in cents, from 0 to subtotalCents
     */
    long discountCents(long subtotalCents);
}

record PercentOff(int percent) implements Discount {
    public long discountCents(long subtotalCents) {
        return subtotalCents * percent / 100;
    }
}

/** "5 euros off". */
record FlatCoupon(long cents) implements Discount {
    public long discountCents(long subtotalCents) {
        return cents;
    }
}

/** "5 euros off orders over 20 euros". */
record MinimumSpendCoupon(long cents, long minimumCents) implements Discount {
    public long discountCents(long subtotalCents) {
        if (subtotalCents < minimumCents) {
            throw new IllegalArgumentException("subtotal below minimum spend");
        }
        return cents;
    }
}
```

Each coupon passed its own unit test, written with a cart big enough for it. The contract is
checked once, against the interface:

```java
static String contract(Discount d) {
    StringBuilder broken = new StringBuilder();
    for (long subtotal : new long[] {0, 300, 2_500, 90_000}) {
        try {
            long off = d.discountCents(subtotal);
            if (off < 0 || off > subtotal) broken.append(" ").append(subtotal).append("->").append(off);
        } catch (RuntimeException e) {
            broken.append(" ").append(subtotal).append("->").append(e.getClass().getSimpleName());
        }
    }
    return broken.isEmpty() ? "keeps the contract" : "breaks it at" + broken;
}
```

```text
Discount contract
  PercentOff(10)                 keeps the contract
  FlatCoupon(500)                breaks it at 0->500 300->500
  MinimumSpendCoupon(500, 2000)  breaks it at 0->IllegalArgumentException 300->IllegalArgumentException
```

`FlatCoupon` weakens the postcondition: a 3-euro cart checks out at minus 2 euros.
`MinimumSpendCoupon` strengthens the precondition: it rejects carts the interface says every
discount accepts, so the cart page throws for an empty cart with a coupon applied. The fixes cap
both coupons with `Math.min(cents, subtotalCents)`, and the minimum-spend one returns 0 below its
minimum. The same check passes:

```text
Discount contract, fixed
  PercentOff(10)                 keeps the contract
  FlatCoupon(500)                keeps the contract
  MinimumSpendCoupon(500, 2000)  keeps the contract
```

The opposite changes pass. `PercentOff(10)` promises more than the interface, never above a tenth
of the subtotal, and keeps the contract. A discount that also accepted a negative subtotal would
break no caller, since none written against the interface sends one. An implementation that can't
serve every input the interface allows needs an interface of its own (unit 8.1.4).

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **javac checks an override's signature, not its contract.** Accepted inputs, results and
  unchecked exceptions are checked only by a caller test run on every implementation.
- **`List`'s mutators are optional operations.** `List.of` keeps the contract by throwing; a
  method that changes the list it was given is the code in breach, and copies instead.
- **`Timestamp` breaks `equals` symmetry with `Date`.** Mixed in one collection, the count of
  duplicates depends on arrival order; convert to `Instant` at the boundary.
- **A subtype may accept more and promise more, never less.** Rejecting an input the supertype
  accepts, or returning outside its range, breaks every caller that trusted the supertype.

## Reference

**What javac enforces on an override (Java 21):**

| Change in the override | javac |
|---|---|
| return type narrowed (`String` for `Object`) | accepted |
| a checked exception the overridden method doesn't declare | error: "overridden method does not throw" |
| access narrowed (`public` to package-private) | error: "attempting to assign weaker access privileges" |
| a new unchecked exception | accepted |

**JDK subtypes and the contract they keep (Java 21):**

| Case | Behaviour | Contract |
|---|---|---|
| `List.of`, `List.copyOf`, `stream().toList()`, `Collections.unmodifiableList` | every mutator throws `UnsupportedOperationException` | kept: mutators are optional |
| `Arrays.asList` | `set` and `sort` work; `add` and `remove` throw | kept: fixed-size |
| `List.of(...).contains(null)`, `Set.of(...).contains(null)` | `NullPointerException` | kept: marked optional |
| `ArrayList.contains(null)`, `stream().toList().contains(null)` | `false` | kept |
| `Timestamp.equals(Date)` | `false` where `Date.equals(Timestamp)` is `true` | broken: javadoc says not symmetric |
| `TreeSet<Date>` given a `Date` and a `Timestamp` of the same millisecond with extra nanoseconds | 2 elements or 1, by insertion order | broken: `compareTo` disagrees in the same way |

## Challenges

1. A library method takes `Collection<String>` and must return it sorted without duplicates. Would
   you change the parameter type, copy, or document a precondition? What does each cost a caller?
2. Write the contract test for `Map.get` as your service uses it. Which of `HashMap`,
   `ConcurrentHashMap`, `Map.of` and `TreeMap` pass it with a null key?
3. Your team's `Repository` interface has a `delete` that one read-only implementation can't
   support. List the options and which callers each one breaks.

## Design Note: Why the Collection Interfaces Have Optional Operations

The collections framework could have given each kind of collection several interfaces: modifiable,
fixed-size, unmodifiable, null-tolerant, each for `List`, `Set` and `Map`. It kept one type per kind
and marked the mutators optional, so one interface covers every list. The type then no longer
says which calls will work, and a caller finds out at run time. Splitting an interface by what its
clients need is unit 8.1.4.

## Code Examples

- Source: [LiskovSubstitution.java](src/main/java/com/github/msorkhpar/claudejavatutor/solidprinciples/LiskovSubstitution.java)
- Test: [LiskovSubstitutionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/solidprinciples/LiskovSubstitutionTest.java)
