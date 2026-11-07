# 8.2.1. Definition and Purpose of KISS Principle

On a Friday afternoon the shop reprices its best-selling mug from 18.99 to 21.49. Half an hour
later checkout still charges 18.99, although the price cache's time to live is five minutes. The
cache class is sixty lines: per-SKU entries, LRU eviction, hit counters. The pricing team's
requirement is one sentence: a price shown at checkout is never more than five minutes old.

KISS ("keep it simple") asks for the simplest code that meets a stated requirement. The caches on
this page are measured against that requirement written as a check, so "simple" means "passes the
check with the fewest moving parts", not "short".

## Expiry Counted From the Last Read Keeps a Popular Price Forever

Read `price()` and decide which SKU goes stale: the one read once an hour, or the one read every
minute?

```java
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** The checkout's price cache: per-SKU entries, a time to live, LRU eviction, hit counting. */
public class PriceCache {
    private record Entry(BigDecimal price, Instant touched) {}

    private final Function<String, BigDecimal> source;
    private final InstantSource clock;
    private final Duration ttl;
    private final int maxEntries;
    private long hits, misses;

    private final Map<String, Entry> entries;

    public PriceCache(Function<String, BigDecimal> source, InstantSource clock, Duration ttl, int maxEntries) {
        this.source = source;
        this.clock = clock;
        this.ttl = ttl;
        this.maxEntries = maxEntries;
        this.entries = new LinkedHashMap<>(16, 0.75f, true) {     // access order: least recently used first
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Entry> eldest) {
                return size() > PriceCache.this.maxEntries;
            }
        };
    }

    public BigDecimal price(String sku) {
        Instant now = clock.instant();
        Entry e = entries.get(sku);
        if (e != null && now.isBefore(e.touched().plus(ttl))) {
            hits++;
            entries.put(sku, new Entry(e.price(), now));          // keep hot entries alive
            return e.price();
        }
        misses++;
        BigDecimal price = source.apply(sku);
        entries.put(sku, new Entry(price, now));
        return price;
    }

    /** One line per cached SKU, for the admin page. */
    public String report() {
        StringBuilder out = new StringBuilder();
        for (String sku : entries.keySet()) {
            out.append(sku).append(' ').append(entries.get(sku).price()).append('\n');
        }
        return out.toString();
    }

    public String stats() { return "hits=" + hits + " misses=" + misses + " size=" + entries.size(); }
}
```

The check drives any `sku -> price` function with a fake clock (an `InstantSource`, injected as in
unit 7.3.2): the mug is repriced at minute 1, read every minute to minute 30, and must show 21.49
from minute 7 on.

```java
static Instant now = Instant.parse("2026-03-02T09:00:00Z");
static final Map<String, BigDecimal> catalogue = new HashMap<>(Map.of(
        "MUG-01", new BigDecimal("18.99"), "LAMP-07", new BigDecimal("64.40")));

static void check(String name, Function<String, BigDecimal> checkout) {
    now = Instant.parse("2026-03-02T09:00:00Z");
    catalogue.put("MUG-01", new BigDecimal("18.99"));
    int late = 0;
    BigDecimal shown = null;
    for (int minute = 0; minute <= 30; minute++) {
        if (minute == 1) catalogue.put("MUG-01", new BigDecimal("21.49"));
        shown = checkout.apply("MUG-01");
        if (minute > 6 && !shown.equals(new BigDecimal("21.49"))) late++;   // repriced more than 5 minutes ago
        now = now.plus(Duration.ofMinutes(1));
    }
    System.out.printf("%-7s %-11s at minute 30 shows %s; old price after the 5-minute limit: %d times%n",
            late == 0 ? "ok" : "FAILED", name, shown, late);
}
```

```java
InstantSource clock = () -> now;
PriceCache cache = new PriceCache(catalogue::get, clock, Duration.ofMinutes(5), 10_000);
check("PriceCache", cache::price);
```

```text
FAILED  PriceCache  at minute 30 shows 18.99; old price after the 5-minute limit: 24 times
```

Each hit writes `now` back into the entry, so the five minutes count from the last read, not from
the load. A SKU read at least every five minutes is never reloaded; the one read hourly always is.
The bestseller keeps the old price until reads stop at night or the service restarts.

Nobody asked for "keep hot entries alive". It is a different policy, sliding expiry, written in
because general-purpose caches offer it, and it runs on every hit whether the requirement wants it
or not.

## One Load Time for the Whole List Meets the Requirement

The catalogue has 3,000 SKUs, and the price-list query returns all of them in a few milliseconds.
So there is no need for an entry per SKU: load the whole list, record when, and reload after five
minutes.

```java
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.util.Map;
import java.util.function.Supplier;

/** The whole price list, loaded in one call and reloaded once it is five minutes old. */
public class PriceList {
    private static final Duration MAX_AGE = Duration.ofMinutes(5);

    private final Supplier<Map<String, BigDecimal>> source;
    private final InstantSource clock;
    private Map<String, BigDecimal> prices;
    private Instant loadedAt;

    public PriceList(Supplier<Map<String, BigDecimal>> source, InstantSource clock) {
        this.source = source;
        this.clock = clock;
    }

    public BigDecimal price(String sku) {
        Instant now = clock.instant();
        if (prices == null || !now.isBefore(loadedAt.plus(MAX_AGE))) {
            prices = Map.copyOf(source.get());
            loadedAt = now;
        }
        return prices.get(sku);
    }

    /** One line per SKU, for the admin page. */
    public String report() {
        StringBuilder out = new StringBuilder();
        for (String sku : prices.keySet()) {
            out.append(sku).append(' ').append(prices.get(sku)).append('\n');
        }
        return out.toString();
    }
}
```

```java
PriceList list = new PriceList(() -> new HashMap<>(catalogue), clock);   // a query returns a fresh map
check("PriceList", list::price);
```

```text
ok      PriceList   at minute 30 shows 21.49; old price after the 5-minute limit: 0 times
```

`loadedAt` is written only when the list is loaded, so the age it measures is the age of the
data. There is no eviction because 3,000 prices fit in memory, and no hit counter because nobody
reads one.

The class is single-threaded as written. Shared between request threads it needs one more choice
of construct, which unit 8.2.2 makes; unit 8.2.3 counts the interleavings that choice removes.

## Code Simpler Than the Requirement Is a Bug, Not KISS

A one-line memo is simpler still:

```java
Map<String, BigDecimal> memo = new HashMap<>();
check("memo", sku -> memo.computeIfAbsent(sku, catalogue::get));
```

```text
FAILED  memo        at minute 30 shows 18.99; old price after the 5-minute limit: 24 times
```

`computeIfAbsent` calls the loader only when the key is absent, and nothing removes keys, so the
first price loaded stays until the process restarts. Where `PriceCache` added a rule nobody asked
for, the memo lacks the one rule that was asked for, and the check fails both.

<!-- practice 1 -->

## An Option Nobody Needed Changes What Other Code May Do

`PriceCache` also evicts the least recently used SKU beyond 10,000 entries. The catalogue has
3,000, so eviction never runs, and it looks harmless. The admin page calls `report()`; its test
caches one SKU and passes. Production caches two:

```java
cache.price("MUG-01");
System.out.print(cache.report());
cache.price("LAMP-07");
System.out.print(cache.report());
```

The program prints the exception and two of its frames:

```text
MUG-01 18.99
java.util.ConcurrentModificationException
    at java.base/java.util.LinkedHashMap$LinkedHashIterator.nextNode(LinkedHashMap.java:1023)
    ...
    at PriceCache.report(PriceCache.java:51)
```

The `true` in the `LinkedHashMap` constructor turns on access order, which LRU eviction needs. In
an access-ordered map `get` moves the entry it finds to the end of the order, and the javadoc
counts that as a structural modification. The loop's `entries.get(sku)` reorders the map under its
own iterator, and the fail-fast iterator (unit 6.5.3) throws on the next step. An entry that is
already last doesn't move, so with one entry the test passed.

The same loop over `PriceList`'s map prints both lines, in no specified order:

```text
MUG-01 18.99
LAMP-07 64.40
```

## A Rule Engine for One Form Adds Failures the Plain Method Cannot Have

The signup form needs two checks: the email has an `@` after its first character, and the user is
18 or older. A developer who expects more forms writes a rule engine instead: rules keyed by the
name of the record accessor they read, applied by reflection.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/** Checks any form object against rules keyed by the name of the accessor each rule reads. */
public final class FormRules {
    public static List<String> violations(Object form, Map<String, Predicate<Object>> rules) {
        List<String> failed = new ArrayList<>();
        for (Map.Entry<String, Predicate<Object>> rule : rules.entrySet()) {
            try {
                Object value = form.getClass().getMethod(rule.getKey()).invoke(form);
                if (!rule.getValue().test(value)) failed.add(rule.getKey());
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("cannot read " + rule.getKey(), e);
            }
        }
        return failed;
    }
}
```

Two years later signup is still the only form. Its rules for the engine, and the same two rules
written out:

```java
public record Signup(String email, int age) {}
```

```java
static final Map<String, Predicate<Object>> SIGNUP_RULES = Map.of(
        "email", v -> v instanceof String s && s.indexOf('@') > 0,
        "age", v -> v instanceof Integer n && n >= 18);

static List<String> violations(Signup s) {
    List<String> failed = new ArrayList<>();
    if (s.email().indexOf('@') <= 0) failed.add("email");
    if (s.age() < 18) failed.add("age");
    return failed;
}
```

Both are given `new Signup("jane.example.com", 16)`. Ten runs of the same program, counted:

```text
      5 FormRules: [age, email]
      5 FormRules: [email, age]
     10 plain:     [email, age]
```

The engine returns violations in the map's iteration order, and `Map.of` leaves that order
unspecified: here it changed from one JVM start to the next. The form shows its errors in a
different order after each restart, and a test that asserts the list passes on some runs and fails
on others.

Then the record component is renamed to `contactEmail`. An IDE rename refactoring updates Java
references, so it updates `s.email()` too; a string literal changes only if you also ask it to
search text and accept that occurrence. Renamed by hand in `Signup.java`, the plain version no
longer compiles:

```text
SignupCheck.java:15: error: cannot find symbol
        if (s.email().indexOf('@') <= 0) failed.add("email");
             ^
  symbol:   method email()
  location: variable s of type Signup
1 error
```

Either way, a program that uses only the engine (`EngineCheck`, its `main` validating the same
signup) compiles cleanly, and the first signup fails:

```text
Exception in thread "main" java.lang.IllegalStateException: cannot read email
	at FormRules.violations(FormRules.java:15)
	at EngineCheck.main(EngineCheck.java:10)
Caused by: java.lang.NoSuchMethodException: Signup.email()
	at java.base/java.lang.Class.getMethod(Class.java:2397)
	at FormRules.violations(FormRules.java:12)
	... 1 more
```

The engine turned two method calls into strings, and javac checks no strings. When to share code
between forms that do exist is unit 8.3.4's subject.

## The Plain Version Fits Today's Numbers; When They Change, Replace It

`PriceList` is simple because the list loads fast. Then the catalogue grows. Here is how long each
checkout's `price()` takes, one checkout a minute, with the query replaced by building a map of
that many entries (one machine, Java 21):

```java
long start = System.nanoTime();
list.price("SKU-42");
line.append(' ').append((System.nanoTime() - start) / 1_000_000);
```

```text
     3,000 SKUs, ms per checkout, one a minute: 5 0 0 0 0 3 0 0 0 0 1
 2,000,000 SKUs, ms per checkout, one a minute: 1105 0 0 0 0 1142 0 0 0 0 1293
```

At two million SKUs the first checkout after each five-minute mark waits over a second. If a
requirement says no checkout waits, the next design reloads in the background and swaps the new
map in (unit 8.2.2), with a check of its own for that requirement.

<!-- practice 2 -->

## Summary

- **Expiry counted from the last read keeps a popular price forever.** A general-purpose cache
  brings rules of its own, and sliding expiry broke "never more than five minutes old" for the
  SKU read most.
- **One load time for the whole list meets the requirement.** With 3,000 SKUs that load in
  milliseconds, a map and a timestamp written only on load pass the check.
- **Code simpler than the requirement is a bug.** A `computeIfAbsent` memo never reloads and fails
  the same check.
- **An option nobody needed changes what other code may do.** Access order, turned on for eviction
  that never runs, makes `get` a structural modification, and the report throws CME with two SKUs.
- **A rule engine for one form adds failures the plain method cannot have.** Its order changes
  between runs, and a renamed accessor compiles and fails at the first signup.
- **The plain version fits today's numbers.** At two million SKUs one checkout every five minutes
  waits a second; the replacement is written when that requirement exists.

## Reference

| API | Behaviour (Java 21 javadoc, and the runs above) |
|---|---|
| `new LinkedHashMap<>(cap, load, true)` | access order: `get`, `getOrDefault`, `put`, `putIfAbsent`, `compute*`, `merge` and `replace` on a present key make it the last entry; in this mode `get` is a structural modification |
| `LinkedHashMap.removeEldestEntry` | called after a new mapping is inserted (the javadoc names `put` and `putAll`); returning `true` removes the eldest entry |
| `Map.of`, `Map.copyOf` | unmodifiable; null keys and values rejected; iteration order unspecified and, in these runs, different between JVM starts |
| `HashMap` | keeps a mapping until it is removed or replaced; growing the table keeps every mapping |
| `Map.computeIfAbsent` | calls the function only when the key is absent (or mapped to `null`); a present value is never recomputed |
| `Class.getMethod(name)` | finds a public method by name at run time; a missing one throws `NoSuchMethodException` |

## Challenges

1. The pricing team adds a rule: a price changed by hand in the back office shows at checkout
   within ten seconds. Which of the three designs on this page survives, and what is the simplest
   change that meets the new requirement?
2. Your service already has a cache library on the classpath, with expiry after write as an
   option. Is configuring it simpler than `PriceList`? Count the rules each one brings.
3. Find a class in your codebase with an option set "in case": a pool size, a retry policy, an
   eviction limit. What does it change about the code around it today?

## Design Note: Why the Check Comes Before the Code

"Simple" has no measure on its own. The `computeIfAbsent` memo has fewer lines and branches than
`PriceList` and fails the check; `PriceCache` handles more situations than either and fails it too.
Written first, the requirement's check turns "simplest" into a question with an answer: the least
code that passes it.

The cost is that the plain design is chosen for today's numbers and replaced, not extended, when
they change. The alternative is a general design on day one, which carries rules nobody requested
(sliding expiry, access order, string-keyed rules) from the start, each with behaviour no check
was written for.

## Code Examples

- Test: [KissDefinitionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissDefinitionTest.java)
- Source: [KissDefinition.java](src/main/java/com/github/msorkhpar/claudejavatutor/kissprinciple/KissDefinition.java)
