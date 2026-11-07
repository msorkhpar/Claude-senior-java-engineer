# 2.3.3 Abstraction in Java

Payments library 3.2 adds `refund` to its `PaymentGateway` interface. A merchant's plugin, built
against 3.1, handles checkout all week and throws `AbstractMethodError` at the first refund. In the
same release of the order service, the audit trail of orders starts listing refunds, and the
nightly export that sums 100,000 amounts takes three seconds on one kind of list and three
milliseconds on another.

An abstraction is a type that says what an object does and hides how. Each ticket here is
something a type shared, broke or hid without its authors meaning it to.

## An Interface Can't Hold an Object's State: a Field Declared There Is One Constant for Every Implementation

The order and refund services both keep an audit trail, so someone writes it once, as an interface
with default methods:

```java
import java.util.ArrayList;
import java.util.List;

interface Audited {
    List<String> events = new ArrayList<>();         // looks like a field of each object

    default void audit(String event) {
        events.add(event);
    }

    default List<String> history() {
        return events;
    }
}

class OrderService implements Audited { }
class RefundService implements Audited { }
```

The order service audits `order 1001 placed`, the refund service `refund 77 issued`, and each
prints its history:

```text
orders:  [order 1001 placed, refund 77 issued]
refunds: [order 1001 placed, refund 77 issued]
```

Every field of an interface is implicitly `public static final`. There is one `events` list,
created when the interface is initialized, and every implementing object reaches the same one.
With an `int count`, `count++` in a default method fails to compile: `cannot assign a value to
static final variable count`. A mutable list compiles, and every implementer shares it.

An interface has no instance fields and no constructor, so a default method has no state of its
own: it works through the methods it can call on `this`. Each implementing class can keep its own
list behind an abstract `events()` method, but state that the shared code owns and initializes
needs an abstract class, which can also have constructors and `protected` members (unit 2.2.1).
The cost: a class extends one class, but implements any number of interfaces. The JDK often ships
both: `List` is the type callers use, and `AbstractList` is a skeleton that implements most of it
on top of `get` and `size`, for implementers with no other superclass.

## An Abstract Class Can't Be Instantiated, but Its Constructor Runs for Every Subclass

Every account must carry an ISO currency code, whatever kind of account it is:

```java
import java.math.BigDecimal;

abstract class Account {
    private final String currency;
    private BigDecimal balance = BigDecimal.ZERO;

    protected Account(String currency) {
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("not an ISO currency: " + currency);
        }
        this.currency = currency;
        System.out.println("Account(" + currency + ") ran for " + getClass().getSimpleName());
    }

    protected abstract BigDecimal fee(BigDecimal amount);

    public final void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount).subtract(fee(amount));
    }

    @Override
    public String toString() {
        return balance + " " + currency;
    }
}
```

`new Account("EUR")` doesn't compile (`Account is abstract; cannot be instantiated`). A
`Checking` subclass whose fee is 0.50, and an anonymous subclass written in a test:

```java
Account checking = new Checking("EUR");
checking.withdraw(new BigDecimal("20"));
System.out.println(checking);
Account trial = new Account("eur") {                 // an anonymous subclass
    @Override
    protected BigDecimal fee(BigDecimal amount) {
        return BigDecimal.ZERO;
    }
};
```

```text
Account(EUR) ran for Checking
-20.50 EUR
Exception in thread "main" java.lang.IllegalArgumentException: not an ISO currency: eur
```

`abstract` forbids only `new Account(...)` itself. Every subclass constructor starts with
`super(...)` or `this(...)` (unit 2.1.2), so every chain passes through `Account`'s, even the
anonymous class's, which javac writes for you. A check in the abstract constructor holds for
subclasses that don't exist yet. Put the check in `Checking` instead, and the next team's `Savings`
accepts `"eur"`. Only a subclass can run an abstract class's constructor, even a `public` one;
`protected` says so to the reader. Calling an abstract method from it runs the subclass's override
before the subclass's fields are set (unit 2.1.1).

<!-- practice 1 -->

## Adding an Abstract Method to a Published Interface Breaks Every Implementer; a Default Method Doesn't

The gateway from the opening, at 3.2:

```java
public interface PaymentGateway {
    String charge(String order, long cents);

    String refund(String order, long cents);         // new in 3.2
}
```

The merchant's `AcmeGateway` implements `charge` and was compiled against 3.1. Checkout, rebuilt
against 3.2, never names `AcmeGateway` in its source: it loads the class by name, charges an order
and refunds it:

```text
charged A-1001 4999
java.lang.AbstractMethodError: Receiver class AcmeGateway does not define or inherit an implementation of the resolved method 'abstract java.lang.String refund(java.lang.String, long)' of interface PaymentGateway.
```

The JVM loaded and linked `AcmeGateway` without a complaint. It looks for the implementation only
when the method is called, so the plugin passed every checkout and failed at the first refund,
days after the deploy. Rebuilding the plugin fails instead, at compile time: `AcmeGateway is not
abstract and does not override abstract method refund(String,long) in PaymentGateway`. Adding an
abstract method to an abstract class gives the same `AbstractMethodError`.

A default method gives the old classes a body:

```java
public interface PaymentGateway {
    String charge(String order, long cents);

    default String refund(String order, long cents) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " can't refund");
    }
}
```

```text
charged A-1001 4999
java.lang.UnsupportedOperationException: AcmeGateway can't refund
```

The old plugin still can't refund, but it fails with the library's message, and it compiles
against 3.2 unchanged. `Iterator.remove`'s default also throws `UnsupportedOperationException`.
A default built from the interface's existing methods, like `atLeast` below, is better still. A
default that collides with one the class inherits from another interface breaks it too, at compile
time or with `IncompatibleClassChangeError` (unit 2.3.1).

## A Functional Interface Is the Lightest Abstraction: One Abstract Method, and Any Lambda Implements It

When what varies is one operation, an interface with one abstract method replaces a class
hierarchy, and each variant is a lambda:

```java
@FunctionalInterface
interface FeeRule {
    long feeCents(long amountCents);

    default FeeRule atLeast(long minCents) {               // defaults don't count
        return amount -> Math.max(minCents, feeCents(amount));
    }

    boolean equals(Object other);                          // nor do Object's methods
}
```

```java
FeeRule card = amount -> amount * 29 / 1000 + 30;        // 2.9% + 30 cents
FeeRule wire = amount -> 1500;
FeeRule cardMin = card.atLeast(50);
```

```text
   500: card 44, card with minimum 50, wire 1500
 20000: card 610, card with minimum 610, wire 1500
```

A lambda can implement an interface that has exactly one abstract method, not counting public
methods of `Object`: every object already has `equals`, so declaring it again asks nothing new of a
lambda. `Comparator` declares `equals` this way and is still functional.

An interface with one abstract method is functional with or without `@FunctionalInterface`. The
annotation makes javac check the interface itself, so a second abstract method fails where it was
added:

```text
FeeRule.java:1: error: Unexpected @FunctionalInterface annotation
@FunctionalInterface
^
  FeeRule is not a functional interface
    multiple non-overriding abstract methods found in interface FeeRule
```

Without it, the same change compiles, and every lambda in every caller's code fails instead:
`incompatible types: FeeRule is not a functional interface`. Before writing one, look in
`java.util.function`: `LongUnaryOperator` has `FeeRule`'s shape. Write your own for a name from
the domain, or for defaults such as `atLeast`.

<!-- practice 3 -->

## An Abstraction Hides Its Cost Too: List.get(i) Is O(n) on a LinkedList

The nightly export sums 100,000 amounts, and was written against `List<Long>`:

```java
static long byIndex(List<Long> amounts) {
    long total = 0;
    for (int i = 0; i < amounts.size(); i++) total += amounts.get(i);
    return total;
}

static long byIterator(List<Long> amounts) {
    long total = 0;
    for (long amount : amounts) total += amount;
    return total;
}
```

```text
ArrayList  RandomAccess=true  get(i) loop     3 ms, for-each  4 ms
LinkedList RandomAccess=false get(i) loop  2784 ms, for-each  5 ms
```

`List` promises what `get(i)` returns, not what it costs. A `LinkedList` walks to index `i` from the
nearer end, so the index loop takes about n²/4 link steps, 2.5 billion for 100,000 amounts, and
the for-each walks the links once. Nothing in the export's code or tests says which list it gets,
and the time grows with the square of the size: twice the amounts, four times the wait.

That is a leaky abstraction: to use the type well, you have to know what is behind it. For this
leak the JDK has a marker interface, `RandomAccess`: `ArrayList` implements it and `LinkedList`
doesn't, and generic algorithms check it. `Collections.binarySearch` on a large list without it
switches to an iterator-based search. Code that accepts any `List` should iterate, and use indexes
only when the list is `RandomAccess`.

<!-- practice 2 -->

## Summary

- **An interface field is one `static final` value for every implementation.** Per-object state
  needs an abstract class, at the cost of the single superclass.
- **An abstract class's constructor runs for every subclass**, anonymous ones included, so the
  checks every subclass needs go there.
- **A new abstract method breaks every implementer**: rebuilt ones don't compile, old binaries
  throw `AbstractMethodError` at the first call. A default method keeps both working.
- **One abstract method, not counting `Object`'s, makes an interface functional.**
  `@FunctionalInterface` moves the error for a second one to the interface.
- **`List.get(i)` costs O(n) on a `LinkedList`.** Iterate, or check `RandomAccess` before using
  indexes.

## Reference

**What each can declare (javac 21):**

| Member | Interface | Abstract class |
|---|---|---|
| instance field | no: every field is `public static final` | yes |
| constructor | no | yes, runs for every subclass |
| `protected` member | no: `modifier protected not allowed here` | yes |
| `private` method | yes, since Java 9, to share code between defaults | yes |
| `static` method | yes, called as `Iface.m()`; not inherited, so `Impl.m()` doesn't compile | yes, and inherited |
| method with a body | `default` (public), `static`, or `private` (instance or static) | any access |
| how many a class can have | any number | one |

**Adding a method to a published type (Java 21 and 25):**

| Change | Implementer rebuilt | Implementer not rebuilt |
|---|---|---|
| abstract method to an interface | compile error | `AbstractMethodError` at the first call |
| abstract method to an abstract class | compile error | `AbstractMethodError` at the first call |
| default method to an interface | compiles, unless it conflicts with another default (unit 2.3.1) | the default runs; a conflicting one throws `IncompatibleClassChangeError` |

## Challenges

1. Your library's interface has forty implementers in other teams' repositories, and you need an
   operation that can't be built from the existing ones. A default that throws, a new
   sub-interface, or a major version: what does each cost the implementers and the callers?
2. An abstract `Repository` base class with twelve subclasses holds a connection pool in a field.
   Could it become an interface with defaults? What would have to move, and where?
3. Find a method in your code that takes a `List` and loops by index. Which implementations reach
   it, and would you add a `RandomAccess` check, or change the loop?

## Design Note: Why Java 8 Added Default Methods

Java 8 wanted `forEach` on `Iterable`, and `stream` and `removeIf` on `Collection`. As abstract
methods they would have broken every collection outside the JDK: a compile error for the ones
rebuilt, `AbstractMethodError` for the ones that weren't. Default methods let an interface grow.

The cost is that an interface now carries behaviour it can only build from the methods it can
call on `this`, since it sees no state. `Collection.removeIf`'s default walks `iterator()` and
calls `Iterator.remove`. On a collection whose iterator can't remove, it throws
`UnsupportedOperationException` at the first element that matches. A class with a faster way, or with a lock to take first, has to
know that the default exists and override it. Two interfaces can now bring the same method
with two bodies, which unit 2.3.1 resolves.

## Code Examples

- Test: [AbstractionTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/oo/AbstractionTest.java)
- Source: [Vehicle.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Vehicle.java)
- Source: [Drivable.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Drivable.java)
- Source: [Car.java](src/main/java/com/github/msorkhpar/claudejavatutor/oo/Car.java)
