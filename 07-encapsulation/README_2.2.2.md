# 2.2.2 Getter and Setter Methods in Java

A guest asks the front desk to move a stay from 10–12 March to 20–22 March. The app calls
`setCheckIn`, then `setCheckOut`, and the move is refused:
`check-in 2026-03-20 is not before check-out 2026-03-12`. In the same order, moving a stay
earlier works. Both setters validate.

## A Setter Can Check Its Own Value, Not a Rule That Spans Two Fields

`Stay` keeps check-in before check-out. Each setter checks its new value against the other
field; `reschedule` takes both:

```java
import java.time.LocalDate;

public class Stay {
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Stay(LocalDate checkIn, LocalDate checkOut) {
        reschedule(checkIn, checkOut);
    }

    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }

    public void setCheckIn(LocalDate checkIn) {
        if (!checkIn.isBefore(checkOut))
            throw new IllegalArgumentException("check-in " + checkIn + " is not before check-out " + checkOut);
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut))
            throw new IllegalArgumentException("check-out " + checkOut + " is not after check-in " + checkIn);
        this.checkOut = checkOut;
    }

    public void reschedule(LocalDate checkIn, LocalDate checkOut) {
        if (!checkIn.isBefore(checkOut))
            throw new IllegalArgumentException("check-in " + checkIn + " is not before check-out " + checkOut);
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    @Override public String toString() { return checkIn + ".." + checkOut; }
}
```

Before reading on: for a move later, which order of the two setters works? For a move earlier?

```text
later, in then out: check-in 2026-03-20 is not before check-out 2026-03-12, stay is 2026-03-10..2026-03-12
later, out then in: 2026-03-20..2026-03-22
earlier, out then in: check-out 2026-03-03 is not after check-in 2026-03-10, stay is 2026-03-10..2026-03-12
reschedule later: 2026-03-20..2026-03-22
reschedule earlier: 2026-03-01..2026-03-03
```

A setter compares its value with the fields as they are now, and in the middle of a two-step
change one of them still holds the old date. The order that works depends on the direction of
the move, so a caller that learns "out first" breaks the other way. A rule over two fields
belongs in one method that receives both values.

That method checks every argument before it assigns any field. A version that assigns `checkIn`
first and then throws leaves the stay as `2026-03-20..2026-03-12`, a state no setter would have
accepted.

<!-- practice 1 -->

## A Getter That Returns a Date or an Array Hands Out the Field

Unit 1.3.2 showed a getter that returns its internal list. `java.util.Date` and arrays leak the
same way, and `private final` only stops reassignment. A signed contract has these getters:

```java
public Date getSignedAt() { return signedAt; }
public String[] getApprovers() { return approvers; }
public List<String> approverList() { return Arrays.asList(approvers); }
```

```java
Contract c = new Contract(new Date(1_767_225_600_000L), "ana", "bo");
c.getSignedAt().setTime(0);                 // a report "normalises" the date it was given
c.getApprovers()[1] = "mallory";            // a caller edits the array it got back
c.approverList().set(0, "eve");             // Arrays.asList is a view of the array
```

```text
signed:  2026-01-01T00:00:00Z [ana, bo]
after:   1970-01-01T00:00:00Z [ana, mallory]
asList:  1970-01-01T00:00:00Z [eve, mallory]
```

The contract prints its date with `toInstant()`. It copied both arguments in its constructor
(unit 1.3.3), and its getters still handed out the fields. `Arrays.asList` copies nothing: its `set` writes into the array. Return
`new Date(signedAt.getTime())` and `approvers.clone()`, or store an `Instant` and a `List.of(...)`
copy, which no caller can change, and return them as they are.

<!-- practice 2 -->

## Tools Find Properties by Method Name, and isX Works Only for a Primitive boolean

JavaBeans tools, including the JDK's `java.beans.Introspector`, look for methods named `getX`,
`isX` and `setX`, not for fields. A class `Flags` has these accessors:

```java
public boolean isActive() { return active; }
public void setActive(boolean active) { this.active = active; }

public Boolean isEnabled() { return enabled; }
public void setEnabled(Boolean enabled) { this.enabled = enabled; }

public String getURL() { return url; }
public void setURL(String url) { this.url = url; }

public int getPort() { return port; }
public Flags setPort(int port) { this.port = port; return this; }   // fluent

public double getArea() { return width * height; }
```

`Introspector.getBeanInfo(type, Object.class)`, which leaves out `getClass()`, on `Flags` and on
`record Endpoint(String host, int port)`:

```text
Flags:
  URL      read=getURL()   write=setURL()
  active   read=isActive() write=setActive()
  area     read=getArea()  write=-
  enabled  read=-          write=setEnabled()
  port     read=getPort()  write=-
Endpoint:
```

`is` names a getter only when it returns a primitive `boolean`, so `enabled` can be written and
never read. A setter must return `void`: the fluent `setPort` is invisible, and a binder built on the
introspector leaves every port at its default. `getArea()` has no field behind it and is still a read-only property.
`getURL` gives the property `URL`, not `url`: a name whose first two letters are capitals keeps
its case. A record's accessors have no prefix, so the introspector finds no property in `Endpoint`.

> **Libraries differ.** Jackson, Spring and others apply their own versions of these rules.
> Check the one you bind with.

## A Record's Accessor Is name(), and Overriding It Doesn't Change equals

A record's accessor may be declared explicitly, as long as it is `public` with the component's
type. This one normalises a tag:

```java
import java.util.Locale;

record Tag(String name) {
    @Override public String name() { return name.strip().toLowerCase(Locale.ROOT); }
}
```

Two tags, `new Tag("Java")` and `new Tag(" java ")`:

```text
names:  java / java
equals: false, toString: Tag[name=Java] / Tag[name= java ]
set size: 2
pattern sees: [java]
copy equals original: false
```

The generated `equals`, `hashCode` and `toString` read the fields, not the accessors, so two tags
that report the same name are two entries in a set. A record pattern `Tag(String n)` calls the
accessor and sees `java`. `new Tag(b.name())` is not equal to `b`, which breaks the contract
in `Record.equals`'s javadoc: a record rebuilt from its accessors must equal the original.

Normalise in the compact constructor instead, so the field holds the value every method sees:

```java
import java.util.Locale;

record NormalTag(String name) {
    NormalTag { name = name.strip().toLowerCase(Locale.ROOT); }
}
```

```text
compact: true, NormalTag[name=java] / NormalTag[name=java]
```

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **A setter checks one value against the current state.** A rule spanning two fields needs one
  method that validates both arguments, then assigns.
- **A getter that returns a `Date` or an array hands out the field.** Return a copy, or store an
  immutable type.
- **Tools find properties by method name.** `isX` only for `boolean`, setters return `void`, and
  a record's accessors have no prefix.
- **A record's `equals` and `toString` read fields, not accessors.** Normalise in the compact
  constructor.

## Reference

**JavaBeans names, as `Introspector` applies them (Java 21):**

| Method | Property |
|---|---|
| `T getX()` | `x`, readable |
| `boolean isX()` | `x`, readable |
| `Boolean isX()` | not a getter |
| `void setX(T)` | `x`, writable |
| `Self setX(T)` (fluent) | not a setter |
| `getURL()` | `URL`: first two letters capital, case kept |
| record accessor `x()` | not a property |

**Record accessors:** an explicit accessor must be `public` and return the component's type;
`javac` rejects `String code()` without `public`.

## Challenges

1. A `Meeting` has `start`, `end` and `room`, and a move must keep the room free. Which method
   owns that check, and what does a caller that only changes the room call?
2. Your JSON library binds a `Boolean isArchived()`. Find out whether it reads it, and what it
   writes for `null`.
3. A record holds a `List<String>`. Do you copy it in the compact constructor, in the accessor, or
   both, and what does each choice do to `equals`?

## Design Note: Why Records Didn't Adopt getX

A record's state is its components: the canonical constructor takes them, the accessors return
them under the same names, and `new R(r.a(), r.b())` rebuilds it. The JavaBeans prefix was a
convention for tools to discover properties to read and write, and a record has nothing to write.
The cost falls on tools built on that convention: a record bound by one needs a `getHost()` added
by hand.

## Code Examples

- Test: [PersonTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/encapsulation/PersonTest.java)
- Source: [Person.java](src/main/java/com/github/msorkhpar/claudejavatutor/encapsulation/Person.java)
