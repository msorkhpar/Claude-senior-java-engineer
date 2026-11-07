# 2.4.1 Defining and Using Records in Java

The report service caches rendered PDFs in a `HashMap`, keyed by
`record CacheKey(String tenant, byte[] digest)`, where `digest` is the SHA-256 of the report's
parameters. The cache has never had a hit. Each request computes the digest, builds a key, misses,
renders the PDF again and adds one more entry, and the debug log prints keys like
`CacheKey[tenant=acme, digest=[B@4d405ef7]`.

A record is a class whose state is the list of components in its header. From that list the
compiler writes a private final field and an accessor for each component (unit 2.2.2), a canonical
constructor that takes them in order, and `equals`, `hashCode` and `toString`. Every output is from
Java 21.

## A Record's equals Compares Each Component with Its Own equals, So an Array Compares by Identity

```java
record CacheKey(String tenant, byte[] digest) {}
```

```java
Map<CacheKey, String> cache = new HashMap<>();
cache.put(new CacheKey("acme", sha256("report-q3")), "rendered PDF");

CacheKey again = new CacheKey("acme", sha256("report-q3"));
System.out.println("lookup: " + cache.get(again));
System.out.println("equals: " + again.equals(new CacheKey("acme", sha256("report-q3"))));
System.out.println("key: " + again);
```

```text
lookup: null
equals: false
key: CacheKey[tenant=acme, digest=[B@4d405ef7]
```

The generated `equals` holds when the argument is the same record class and every pair of
components is equal. A reference component is compared with `Objects.equals`, a primitive with its
wrapper's `compare` (the `Record.equals` javadoc). `hashCode` combines the components' hash codes,
and `toString` prints each component's own `toString`. An array inherits `equals` and `hashCode` from `Object`,
so two digests holding the same 32 bytes are two different keys. A `List<Byte>` component would
compare by contents, because `List.equals` does.

Every request renders, and every request adds an entry that no lookup finds, until the heap runs
out. Either store a type that compares by value, such as the digest
as a hex `String`, or override `equals` and `hashCode` with `Arrays.equals` and `Arrays.hashCode`
(and `toString` with `Arrays.toString`). An array is also mutable, so copy it on the way in, as
unit 1.3.3 does for a list, and return a copy from the accessor, which otherwise hands out the
field. Once `hashCode` reads the contents, a caller that changes the array after `put` changes the
key's hash, and the entry is lost in the map.

<!-- practice 1 -->

## The Compact Constructor Runs Before the Fields Are Assigned, So It Checks the Parameters

A compact constructor is the canonical constructor without its parameter list. Its body sees the
components as parameters, and when the body ends, the compiler assigns each parameter to its field.

### Spot the Problem: a Check That Never Fires

```java
record Interval(int start, int end) {
    Interval {
        System.out.println("  in the body: " + this);
        if (end() < start()) throw new IllegalArgumentException(start + " > " + end);
    }
}
```

`System.out.println("accessors: " + new Interval(9, 4))` prints:

```text
  in the body: Interval[start=0, end=0]
accessors: Interval[start=9, end=4]
```

While the body runs, the fields hold their defaults, so the accessors return 0 and the inverted
interval goes through. Any instance method called from the body sees the same zeros and nulls, so a
check that calls a method on a reference component's accessor throws `NullPointerException`.
Written against the parameters, `if (end < start)`, the same constructor throws
`IllegalArgumentException: 9 > 4`.

`javac` refuses the other ways to reach the fields: `this.start = ...` in the body is `cannot
assign a value to final variable start`, and reading `this.start` is `variable start might not have
been initialized`. To change a value before it is stored, assign the parameter, as unit 2.2.2 does
with `name = name.strip().toLowerCase(Locale.ROOT)`. Check the value after normalising it: a check
for an empty name placed before `strip()` lets `"   "` through, and stores it as `""`.

## A Custom Constructor Must Call the Canonical One First, So Checks Belong in the Canonical One

This `Interval` parses `"4-9"`, and checks the order in the constructor that parses:

```java
record Interval(int start, int end) {
    Interval(String text) {
        this(Integer.parseInt(text.split("-")[0]), Integer.parseInt(text.split("-")[1]));
        if (end < start) throw new IllegalArgumentException(text);
    }
}
```

```text
text:      java.lang.IllegalArgumentException: 9-4
canonical: Interval[start=9, end=4]
```

`new Interval("9-4")` is refused, and `new Interval(9, 4)` is not. A record may have other
constructors, but each must start with `this(...)`. One that assigns the fields itself fails with
`constructor is not canonical, so its first statement must invoke another constructor of class
Interval`. Every path to a record therefore ends in the canonical constructor: `new` with all
components, the other constructors, and Java serialization, which calls the canonical constructor
and nothing else (unit 2.2.3 shows it refusing a forged stream).

A check placed after `this(...)`, or in a factory, guards only the callers of that one path. Code
that builds an `Interval` from two database columns, or deserializes one from a queue, gets past
it. Put every check in the canonical constructor, compact or explicit, and let the other
constructors only convert their arguments.

<!-- practice 2 -->

## A Record's Body Adds Methods, Static Members and Interfaces, but No Instance Field

```java
import java.util.Comparator;

interface Identified {
    String id();
}

record Customer(String id, String email) implements Identified {}

record Vendor(String id, String country) implements Identified, Comparable<Vendor> {
    static final Comparator<Vendor> ORDER =
            Comparator.comparing(Vendor::country).thenComparing(Vendor::id);

    static Vendor domestic(String id) { return new Vendor(id, "NL"); }

    @Override public int compareTo(Vendor o) { return ORDER.compare(this, o); }
}
```

```java
List<Identified> all = List.of(new Customer("c-17", "jane@example.com"),
        Vendor.domestic("s-03"), new Vendor("s-01", "BE"));
System.out.println(all.stream().map(Identified::id).toList());
System.out.println(List.of(Vendor.domestic("s-09"), new Vendor("s-02", "BE")).stream().sorted().toList());
```

```text
[c-17, s-03, s-01]
[Vendor[id=s-02, country=BE], Vendor[id=s-09, country=NL]]
```

`Customer` has no body and still implements `Identified`: its generated accessor `id()` is public
with the interface method's signature, so it is the implementation. `java.lang.Record`, which every
record extends, declares only `equals`, `hashCode` and `toString`. An instance field can't go in
the body: `private int hits;` is `field declaration must be static`, and `javac` suggests a record
component instead. All instance state is in the header, so all of it passes through the canonical
constructor and takes part in `equals`, `hashCode` and `toString`. A record can't extend a class;
unit 2.4.3 covers what that rules out.

## Nested and Local Records Are Static, So They Can't Reach an Outer Instance or a Local Variable

```java
class Order {
    private final String currency = "EUR";

    record Line(String sku, long cents) {
        String price() { return cents + " " + currency; }
    }
}
```

```text
InnerCapture.java:5: error: non-static variable currency cannot be referenced from a static context
        String price() { return cents + " " + currency; }
                                              ^
```

A record nested in a class is implicitly `static`: a `Line` has no enclosing `Order`, so
`new Order.Line("TEA-1", 450)` works from a static method, and there is no `currency` to read. A
record declared in a method is static too, unlike a local class or a lambda, so it can't capture
the method's variables, even effectively final ones:

```java
import java.util.List;

class LocalCapture {
    static List<String> labels(List<Integer> starts, int width) {
        record Slot(int start) {
            String label() { return start + "-" + (start + width); }
        }
        return starts.stream().map(s -> new Slot(s).label()).toList();
    }
}
```

```text
LocalCapture.java:6: error: non-static variable width cannot be referenced from a static context
            String label() { return start + "-" + (start + width); }
                                                           ^
```

A hidden reference to an outer object or a captured variable would be state outside the header,
missing from the constructor and from `equals`. Make it a component: with `record Slot(int start,
int width)` and `new Slot(s, width)`, `labels(List.of(0, 30), 15)` returns `[0-15, 30-45]`.

<!-- practice 3 -->

## Summary

- **`equals`, `hashCode` and `toString` use each component's own methods.** An array component
  compares by identity; store a value type or override all three with `Arrays`.
- **The compact constructor runs before the fields are assigned.** Check and normalise the
  parameters; accessors and `this` see default values there.
- **Every constructor ends in the canonical one, and so does Java deserialization.** A check anywhere
  else guards one path.
- **The body adds methods, static members and interfaces, never instance fields.** A matching
  accessor implements an interface method.
- **Nested and local records are static.** Pass what they need as components.

## Reference

**What a record declaration generates (Java 21):**

| Member | What it does |
|---|---|
| field per component | `private final`, assigned once, by the canonical constructor |
| accessor `c()` | `public`, returns the field; an explicit one must be `public` with the component's type |
| canonical constructor | parameters in header order; explicit or compact, at least as accessible as the record |
| `equals(Object)` | same record class, and each component equal as below |
| `hashCode()` | combines the components' hashes; the formula is unspecified |
| `toString()` | `Name[c1=v1, c2=v2]`; the javadoc says the format may change, so don't parse it |

**How the generated `equals` compares one component (ran):**

| Component | Compared by | Result |
|---|---|---|
| `String`, `List`, other reference | `Objects.equals` | by contents if the type's `equals` is; `null` equals `null` |
| array | `Objects.equals`, which is `==` for arrays | equal contents, different arrays: not equal |
| `double`, `float` | `Double.compare`, `Float.compare` | `NaN` equals `NaN`; `0.0` doesn't equal `-0.0` |
| `int` and other primitives | the wrapper's `compare` | by value |

**What `javac` 21 refuses:**

| Code | Message |
|---|---|
| `this.c = ...` in a compact constructor | `cannot assign a value to final variable c` |
| reading `this.c` in a compact constructor | `variable c might not have been initialized` |
| another constructor that doesn't start with `this(...)` | `constructor is not canonical, so its first statement must invoke another constructor of class R` |
| an instance field in the body | `field declaration must be static` |
| a canonical constructor less accessible than the record | `invalid canonical constructor in record R` (`attempting to assign stronger access privileges`) |
| a nested or local record reading an outer field or local variable | `non-static variable x cannot be referenced from a static context` |

## Challenges

1. For the cache key above, would you store the digest as a hex `String`, a `ByteBuffer`, or keep
   `byte[]` and override three methods? Measure each on a million lookups.
2. A record has twelve components and four of them are optional. Do you add constructors, static
   factories or a builder, and where does each one's validation run?
3. A library reads and writes your records by reflection, through their accessors and canonical
   constructor. Which of this page's rules does it rely on, and what breaks if a record overrides
   an accessor?

## Design Note: Why a Record Compares Arrays by Identity

The generated methods are defined by the components alone: `equals` asks each component's
`equals`, with no exception for arrays. An exception would make an array equal by contents inside a
record while the same array in a `List` or a map value is not, and the generated `hashCode` would
hash every byte of a large array on each call. The cost is that a record holding an array has
value syntax and identity equality, and `javac` gives no warning.

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/records/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/records/Person.java)