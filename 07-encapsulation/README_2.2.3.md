# 2.2.3 Data Hiding and Protection in Java

A booking service caches stays as serialized objects. A release made `Stay` refuse zero nights,
and the next run of the invoice job failed with `/ by zero` dividing by `nights()`: the cache held stays the
constructor would never build. Private fields hide data. They protect it only if every way an
object is made, and every way its state changes, goes through the checks.

## A Constructor That Fails Has Already Published this If It Registered Itself First

A meter adds itself to the scheduler's list, then checks its interval:

```java
import java.util.ArrayList;
import java.util.List;

final class Meter {
    static final List<Meter> ACTIVE = new ArrayList<>();   // the scheduler polls every meter here

    private final String id;
    private final int intervalSeconds;

    Meter(String id, int intervalSeconds) {
        ACTIVE.add(this);
        if (intervalSeconds <= 0) throw new IllegalArgumentException("interval " + intervalSeconds);
        this.id = id;
        this.intervalSeconds = intervalSeconds;
    }

    long pollsPerHour() { return 3600 / intervalSeconds; }

    @Override public String toString() { return id + " every " + intervalSeconds + " s"; }
}
```

After `new Meter("m-1", 60)`, `new Meter("m-2", 0)` is refused, and the scheduler then polls what it holds:

```text
refused: interval 0
active: [m-1 every 60 s, null every 0 s]
m-1 every 60 s: 60 polls/h
Exception in thread "main" java.lang.ArithmeticException: / by zero
	at Meter.pollsPerHour(Meter.java:17)
	at Escape.main(Escape.java:10)
```

The object exists before the constructor body runs. The exception stops `new` from returning a
reference, but one already handed out survives, with its fields at their defaults: `null every 0 s`.
The crash comes on every later poll, far from the line that refused the meter. Registering a
listener, starting a thread or caching `this` does the same. Check every argument first, assign,
and publish last, or let a factory publish the finished object.

<!-- practice 1 -->

## An Immutable Class Is final, and Copies Every Mutable Part, Not Just the Collection

`Money` has `private final long cents`, a getter and no setters, so `Invoice` keeps the `Money` it
is given without copying it; `dueCents()` returns `total.cents()`. But `Money` isn't `final`:

```java
static class AdjustableMoney extends Money {
    long cents;
    AdjustableMoney(long cents) { super(cents); this.cents = cents; }
    @Override long cents() { return cents; }
}
```

```java
AdjustableMoney total = new AdjustableMoney(12_000);
Invoice invoice = new Invoice(total);
System.out.println("due: " + invoice.dueCents());
total.cents = 1;                                    // the caller still holds its object
System.out.println("due: " + invoice.dueCents());
System.out.println("BigDecimal final: " + Modifier.isFinal(BigDecimal.class.getModifiers())
        + ", BigInteger final: " + Modifier.isFinal(BigInteger.class.getModifiers())
        + ", String final: " + Modifier.isFinal(String.class.getModifiers()));
```

```text
due: 12000
due: 1
BigDecimal final: false, BigInteger final: false, String final: true
```

`BigDecimal` and `BigInteger` can be subclassed too, so
code that keeps one from a caller it doesn't trust checks its exact class or copies it. Make the
class `final`, or give it only private constructors and static factories.

A `final` field holds a reference, so immutability goes only as deep as the copy. `List.copyOf`
copies the list, not its elements (unit 1.3.3). `Holidays` keeps a list of `java.util.Date`:

```java
Holidays h = new Holidays(input);              // this.days = List.copyOf(days)
input.get(0).setTime(0);                       // the caller reuses its Date
System.out.println(h.days().get(0).toInstant());
```

```text
1970-01-01T00:00:00Z
```

Hold immutable elements (`LocalDate`, `Instant`, `String`), or copy each one in and out. A class
whose fields are all `final` also gets the publication guarantee of unit 5.2.2, provided its
constructor doesn't publish `this`.

<!-- practice 2 -->

## Deserialization Builds the Object Without Your Constructor, So readObject Checks Again

`Stay` refuses fewer than one night in its constructor. A record `StayRecord(int nights)` has the
same check in its compact constructor, and `CheckedStay`, the class again, repeats it in `readObject`:

```java
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    if (nights < 1) throw new InvalidObjectException("nights " + nights);
}
```

We serialize each with three nights, set the four bytes of `nights` in the stream to zero, and
read it back. Both classes print themselves in the record's format:

```text
class: Stay[nights=0]
record: java.io.InvalidObjectException: nights 0, cause java.lang.IllegalArgumentException: nights 0
readObject: java.io.InvalidObjectException: nights 0
```

For a `Serializable` class, `ObjectInputStream` runs only the no-arg constructor of the first
superclass that isn't serializable, here `Object`'s, then sets the fields from the stream. A
record is rebuilt through its canonical constructor. The stream needn't be forged: objects written
before the check existed carry such values too. Throw `InvalidObjectException`, an `IOException`,
so a caller that handles a bad stream handles this one.

A serializable singleton comes back as a second instance, unless a `readResolve()` method returns
the existing one.

<!-- practice 3 -->

## Reflection Can Rewrite a private final Field of a Class, but Not of a Record

`setAccessible(true)` (unit 2.2.1) also gets past `final` in class-path code:

```java
Field f = Stay.class.getDeclaredField("nights");
f.setAccessible(true);
f.setInt(stay, -1);
System.out.println("class field set: nights=" + stay.nights());
```

The same calls, in the program's outer class `Around`, on a record's field and on a
`static final` field set to 365:

```text
class field set: nights=-1
record: java.lang.IllegalAccessException: Can not set final int field Around$StayRecord.nights to (int)-1
static final: java.lang.IllegalAccessException: Can not set static final int field Around$Limits.MAX_NIGHTS to (int)365
```

After `setAccessible(true)` succeeds, `Field.set` writes a `final` field only if it is non-static
and its class is neither a record nor a hidden class. A named module that doesn't open its package
refuses `setAccessible` altogether.

<!-- practice 4 -->

## Summary

- **Check every argument before the constructor publishes `this`.** A reference handed out before
  the throw survives it.
- **An immutable class is `final` and copies every mutable part.** A subclass or a shared element
  changes what callers were told can't change.
- **Deserialization skips your constructor**, records excepted. `readObject` repeats the checks
  and throws `InvalidObjectException`.
- **Reflection can rewrite a `private final` field** of an ordinary class, not of a record. A test
  that does it breaks when the class becomes a record.

## Reference

**What runs when an object is made (Java 21):**

| The object comes from | Constructor that runs |
|---|---|
| `new` | its own, which first runs its superclass's |
| deserializing a `Serializable` class | only the no-arg constructor of the first non-serializable superclass |
| deserializing a record | the canonical constructor |
| deserializing an `Externalizable` class | its public no-arg constructor, then `readExternal` |
| `Object.clone()` | none |

## Challenges

1. A class you own has been serialized into a cache for two years. You add an invariant. What
   happens to the entries already there, and how do you find out before the release?
2. A method of yours keeps a `BigDecimal` from its caller. Copy it, check its class, or accept the
   risk? What does each cost?
3. A test sets a private `final` field by reflection. Which change to the class breaks that test?

## Design Note: Why Serialization Skips the Constructor

Serialization restores an object's fields, private ones included, so it works for classes with no
constructor that takes those fields. The cost is a second way to create objects, one the class's
author doesn't see. Records changed that: their state is their components, so the stream is read
back through the canonical constructor.

## Code Examples

- Test: [BankAccountTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/encapsulation/BankAccountTest.java)
- Source: [BankAccount.java](src/main/java/com/github/msorkhpar/claudejavatutor/encapsulation/BankAccount.java)
