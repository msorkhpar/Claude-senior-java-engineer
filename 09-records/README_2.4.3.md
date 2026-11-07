# 2.4.3. Limitations and Use Cases for Records

The billing team turns `Invoice` into a record to drop its boilerplate. The class extends an
`Audited` base, caches its total in a field, and is loaded by an ORM. The first build fails on the
header, the second on the cache, and once it compiles, the ORM can't load it.

## A Record Extends Only java.lang.Record, and Nothing Extends It

javac 21 on three attempts to fit `Invoice` into a hierarchy:

```text
Invoice.java:5: error: '{' expected
record Invoice(String number, long totalCents) extends Audited {}
                                              ^
Invoice.java:3: error: cannot inherit from final Invoice
class CreditNote extends Invoice {
                         ^
Invoice.java:1: error: modifier abstract not allowed here
abstract record Invoice(String number, long totalCents) {}
         ^
```

A record has no `extends` clause: its superclass is always `java.lang.Record`. It is implicitly
`final`, so it can't be `abstract` either. Its state is exactly its components, which lets the
generated `equals` compare them and `new Invoice(i.number(), i.totalCents())` rebuild an equal
copy. A superclass or a subclass would add state those methods can't see.

Turn `Audited` into an interface; the accessors implement its methods:

```java
interface Audited {
    long createdAtMillis();
    default boolean olderThan(long millis, long nowMillis) { return nowMillis - createdAtMillis() > millis; }
}

record Invoice(String number, long totalCents, long createdAtMillis) implements Audited {}
```

```java
Invoice inv = new Invoice("I-7", 4_200, 1_000);
System.out.println(inv + " older than 1s at t=5000: " + inv.olderThan(1_000, 5_000));
```

```text
Invoice[number=I-7, totalCents=4200, createdAtMillis=1000] older than 1s at t=5000: true
```

The cost is repetition: every `Audited` record declares `createdAtMillis` itself.

<!-- practice 3 -->

## A Record's Only Instance State Is Its Header

javac refuses the old class's cache field, and any instance initializer:

```text
Invoice.java:4: error: field declaration must be static
    private long totalCents = -1;                    // cache, filled on first call
                 ^
  (consider replacing field with record component)
Invoice.java:2: error: instance initializers not allowed in records
```

The error says to make it static. What do two bills print?

```java
import java.util.List;

record Bill(String number, List<Long> lineCents) {
    private static long totalCents = -1;            // javac asked for static, so...

    long totalCents() {
        if (totalCents < 0) totalCents = lineCents.stream().mapToLong(Long::longValue).sum();
        return totalCents;
    }
}
```

```java
Bill a = new Bill("B-1", List.of(1_000L, 250L));
Bill b = new Bill("B-2", List.of(90_000L));
System.out.println("B-1 total: " + a.totalCents());
System.out.println("B-2 total: " + b.totalCents());
```

```text
B-1 total: 1250
B-2 total: 1250
```

A static field belongs to the class, so the first bill's total became every bill's. Compute a derived value in a method on each call; if that is too slow, make it a
component the constructor checks, or use a class. State that changes, such as a retry count, lives
outside the record, for example in a map keyed by it.

## A Framework That Builds an Object Empty, Then Fills It, Can't Build a Record

Many ORMs create an object with its no-arg constructor, then set its fields by reflection.
Jakarta Persistence requires an entity to have a public or protected no-arg constructor, a class
that isn't `final`, and persistent fields that aren't `final`. A record and its fields are final.
A no-arg constructor can be added, but it can only delegate with placeholder values:

```java
record Customer(String name, String email) {
    Customer() { this(null, null); }                // a no-arg constructor must delegate
}
```

What a framework then does, with `record Point(int x, int y)` for comparison:

```java
// Point and Customer are nested in a class Limits
Point.class.getDeclaredConstructor();                           // throws
Customer c = Customer.class.getDeclaredConstructor().newInstance();
Field name = Customer.class.getDeclaredField("name");
name.setAccessible(true);
name.set(c, "Jane Doe");                                        // throws
```

```text
Point no-arg constructor: java.lang.NoSuchMethodException: Limits$Point.<init>()
Customer() built: Customer[name=null, email=null]
set name: Can not set final java.lang.String field Limits$Customer.name to java.lang.String
```

`setAccessible(true)` lets reflection write a final instance field of an ordinary class, but not of a
record. The way in is the canonical constructor, with every value at once.
`getRecordComponents()` gives the components' names and types in header order:

```java
RecordComponent[] comps = Customer.class.getRecordComponents();
Class<?>[] types = Arrays.stream(comps).map(RecordComponent::getType).toArray(Class<?>[]::new);
Constructor<Customer> canonical = Customer.class.getDeclaredConstructor(types);
System.out.println("components: " + Arrays.stream(comps).map(RecordComponent::getName).toList());
System.out.println("built: " + canonical.newInstance("Jane Doe", "jane@example.com"));
```

```text
components: [name, email]
built: Customer[name=Jane Doe, email=jane@example.com]
```

Mappers that support records work this way, and the constructor's checks run on every row. The
entity stays a class; a record carries what is read out of it.

<!-- practice 1 -->

## Keep a Class for Identity, Changing State, or Many Optional Parts

A record's `equals` compares components. Two different visitors with the same name and birth date
check in at two gates:

```java
record Visitor(String name, String birthDate) {}
```

```java
Set<Visitor> inside = new HashSet<>();
boolean north = inside.add(new Visitor("Jane Doe", "1990-04-01"));    // at the north gate
boolean south = inside.add(new Visitor("Jane Doe", "1990-04-01"));    // a different person, south gate
```

```text
north added: true, south added: false, visitors inside: 1
```

The two objects are distinct but equal, so the second `add` returns false and the set of who is inside counts one person. A visitor or an account stays the same thing while its data changes. That is identity, and it needs a class
whose `equals` uses an id, or none. Changing a record makes a new copy, and every other holder of
the old one keeps the old state.

A record's canonical constructor is positional, and with four `int` components a swap compiles:

```java
record PoolConfig(int minSize, int maxSize, int idleTimeoutSeconds, int maxWaitMillis) {}
```

```java
// meant: min 2, max 20, idle 300 s, wait 500 ms
PoolConfig c = new PoolConfig(2, 20, 500, 300);
System.out.println(c);
```

```text
PoolConfig[minSize=2, maxSize=20, idleTimeoutSeconds=500, maxWaitMillis=300]
```

With many components, or many optional ones,
put a builder class or named factory methods in front of the record.

Records fit DTOs and API responses, values such as money, map keys, several results returned
together, and messages taken apart with record patterns (unit 1.5.3).

<!-- practice 2 -->

## Summary

- **A record extends only `java.lang.Record` and is `final`.** Shared shape goes into an interface.
- **A record's only instance state is its header.** A static "cache" is shared by every instance.
- **A framework that constructs empty, then sets fields, can't build a record.** Build it through
  the canonical constructor.
- **Identity, changing state and many optional parts call for a class**, or a builder.

## Reference

**What a record body may declare (javac 21):**

| Declaration | Allowed |
|---|---|
| static fields, static initializers, static and instance methods, nested types | yes |
| `implements`, type parameters | yes |
| a non-canonical constructor | only if it starts with `this(...)` |
| `extends`, `abstract`, instance fields, instance initializers | no |

## Challenges

1. Your events share six components. Compare repeating them in every record with one
   `Envelope(EventId id, Instant at, Payload p)` that wraps a sealed `Payload`. What does each do
   to pattern matching?
2. A costly derived value is read in a sort comparator. Measure recomputing it against storing it
   as a checked component.
3. A row mapper builds records through the canonical constructor. What should it do with a
   missing column for an `int` component?

## Design Note: Why a Record Is Closed

A record promises that its state is its components and nothing else, so `equals`, `hashCode`,
`toString`, deconstruction and serialization are derived from the header and agree. The cost is that a record can't join a class
hierarchy, be filled lazily, or be an entity.

## Code Examples

- Test: [PersonDTOTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/records/PersonDTOTest.java)
- Source: [PersonDTO.java](src/main/java/com/github/msorkhpar/claudejavatutor/records/PersonDTO.java)
