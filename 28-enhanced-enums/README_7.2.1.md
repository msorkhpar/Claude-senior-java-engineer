# 7.2.1. Limitations of Traditional Enums

Release 2 of the order service adds `ON_HOLD` to `OrderStatus`, between `PAID` and `SHIPPED`,
where it reads best. The morning after the deploy, every shipped order shows as on hold and every
delivered one as shipped. Nobody touched the database, and nothing threw. The status column holds
`ordinal()`, which is what JPA stores unless the mapping says `EnumType.STRING`.

An enum is a class with a fixed list of instances, and most of its limits follow from that.

## An Enum's Type Is Closed: It Can't Extend a Class, Take a Type Parameter, or Be Extended

Three designs come up in review: a `Plan` enum that inherits pricing rules from a base class, a
`Key<T>` enum whose constants carry their value type, and a plug-in that adds a `CRITICAL` level
to the core `Severity` enum. None of them compiles (javac 21; the first error of each):

```text
a/Plan.java:3: error: '{' expected
enum Plan extends BaseRule { FREE, PRO }
         ^
b/Key.java:1: error: enums cannot be generic
enum Key<T> { TIMEOUT, NAME }
         ^
c/Severities.java:3: error: cannot inherit from final Severity
class Critical extends Severity { }
                       ^
c/Severities.java:3: error: enum classes are not extensible
class Critical extends Severity { }
^
```

Every enum `E` already extends `java.lang.Enum<E>`, and a class has one superclass, so the enum
grammar has no `extends` clause at all: the first error is a parse error. Writing that superclass
yourself is refused too:

```text
d/MyEnum.java:1: error: classes cannot directly extend java.lang.Enum
abstract class MyEnum extends Enum<MyEnum> { }
         ^
```

The constants are all the instances there are, created when the enum class initialises. A
subclass could add instances that `values()`, `switch` and `EnumSet` don't know, so subclassing
is forbidden. An enum with no constant bodies is implicitly `final`.

The missing type parameter is a language choice, not an effect of erasure: a generic class can
hold `static final` constants with different type arguments. JEP 301 proposed generic enums and
was withdrawn. In Java 21 a constant can't say "my value is an `Integer`". A shared `Object`
field and a cast at every call site is what teams write instead, and a wrong cast fails at the
caller, at run time.

What does work: an enum may implement interfaces, so several enums can share a contract (unit
7.2.3), and typed keys are a final class or a sealed interface with records (unit 7.2.2).

## A Constant With a Body Is an Anonymous Subclass, So getClass() Isn't the Enum

A constant may override a method in a body of its own:

```java
enum Fee {
    FLAT {
        long on(long cents) { return 30; }
    },
    PERCENT {
        long on(long cents) { return cents * 2 / 100; }
    },
    NONE;

    long on(long cents) { return 0; }
}
```

```java
for (Fee f : Fee.values()) {
    System.out.printf("%-8s getClass()=%-12s getDeclaringClass()=%s%n",
            f, f.getClass().getName(), f.getDeclaringClass().getName());
}
```

```text
FLAT     getClass()=Bodies$Fee$1 getDeclaringClass()=Bodies$Fee
PERCENT  getClass()=Bodies$Fee$2 getDeclaringClass()=Bodies$Fee
NONE     getClass()=Bodies$Fee   getDeclaringClass()=Bodies$Fee
```

Each body compiles to a class of its own, `Fee$1` and `Fee$2`, which extends `Fee`. These are the
only subclasses the language allows: each is `final`, and `Fee` becomes implicitly `sealed`,
permitting exactly them (`Fee.class.isSealed()` printed `true`).
`getDeclaringClass()` returns the enum for every constant; `getClass()` returns it only for
constants without a body.

Code that identifies a value by `getClass()` now treats two constants of one enum differently.
A formatter registry keyed by class:

```java
Map<Class<?>, String> formats = new HashMap<>();
formats.put(Fee.class, "fee");
for (Fee f : Fee.values()) {
    System.out.println("format for " + f + ": " + formats.get(f.getClass()));
}
```

```text
format for FLAT: null
format for PERCENT: null
format for NONE: fee
```

The same run printed `false` for `FLAT.getClass().isEnum()` and an empty string for
`FLAT.getClass().getSimpleName()`, so a log line built from it shows no type at all.
`EnumSet.allOf(FLAT.getClass())` doesn't compile, but framework code that holds the class as a raw
`Class` and passes it on gets `ClassCastException: class Bodies$Fee$1 not an enum`. All of this
starts the day someone adds a body to one constant.

What doesn't break: `instanceof Fee`, `switch`, `EnumSet.of(FLAT)`, `EnumMap` keys and
`Enum.valueOf(Fee.class, ...)` all go through the declared enum type. Use `getDeclaringClass()`
wherever code asks "which enum is this?"

<!-- practice 1 -->

## An Ordinal Is a Position, and Inserting a Constant Moves It

`ordinal()` is the constant's index in declaration order. The opening incident, in two releases
and three rows:

```java
/** Release 1. */
enum Status { PLACED, PAID, SHIPPED, DELIVERED }

/** Release 2: ON_HOLD added where it reads best, after PAID. */
enum Status2 { PLACED, PAID, ON_HOLD, SHIPPED, DELIVERED }
```

Release 1 writes `ordinal()` into one column and `name()` into another; release 2 reads both back,
with `Status2.values()[storedOrdinal]` and `Status2.valueOf(storedName)`:

```text
order 4711  stored as 1 -> PAID       stored as PAID      -> PAID
order 4712  stored as 2 -> ON_HOLD    stored as SHIPPED   -> SHIPPED
order 4713  stored as 3 -> SHIPPED    stored as DELIVERED -> DELIVERED
```

Every row after the insertion point now means the constant before it, and every one is a valid
status, so no check fails. Support releases "held" orders that already shipped, and the
warehouse ships them again. Rows written by release 2 use the new meaning; after a rollback to
release 1 they shift again, and old and new rows can't be told apart. Deleting a constant or
sorting the list alphabetically does the same.

Never let an ordinal leave the running process: not into a database column, a message, a cache
or a file. Store `name()`, or a code of your own that you never change (unit 7.2.3 maps a
code back to its constant). Not `toString()`: a constant may override it with display text that
`valueOf` doesn't know. JPA's `@Enumerated` defaults to `EnumType.ORDINAL`, so the safe mapping
has to be written: `@Enumerated(EnumType.STRING)`. `EnumSet` and `EnumMap` use ordinals inside,
which is safe because they never outlive the class.

<!-- practice 2 -->

## valueOf Accepts Only the Exact Name and Throws on Anything Else

Storing names moves the risk to the read. `valueOf` is the generated lookup, and it is strict:

```java
String[] inputs = { "SHIPPED", "shipped", "SHIPPED ", "RETURNED", "", null };
for (String in : inputs) {
    String shown = in == null ? "null" : "\"" + in + "\"";
    try {
        System.out.printf("%-12s -> %s%n", shown, Status.valueOf(in));
    } catch (RuntimeException e) {
        System.out.printf("%-12s -> %s%n", shown, e);
    }
}
```

```text
"SHIPPED"    -> SHIPPED
"shipped"    -> java.lang.IllegalArgumentException: No enum constant ValueOf.Status.shipped
"SHIPPED "   -> java.lang.IllegalArgumentException: No enum constant ValueOf.Status.SHIPPED
"RETURNED"   -> java.lang.IllegalArgumentException: No enum constant ValueOf.Status.RETURNED
""           -> java.lang.IllegalArgumentException: No enum constant ValueOf.Status.
null         -> java.lang.NullPointerException: Name is null
```

It never returns `null`. A name it doesn't know is an `IllegalArgumentException`, but a `null`
name is a `NullPointerException`, so a `catch (IllegalArgumentException e)` around it misses the
nullable column.

The unknown name is the production case. During a rolling deploy, release 3 writes `RETURNED`
while release 2 still serves reads; a page that lists an order's history calls `valueOf` on every
row, and one new row fails the whole page until the rollout ends, or for good after a rollback.
Decide what an unknown name means (skip it, map it to `UNKNOWN`, or return an empty `Optional`) at
the one boundary where names enter, not by letting the exception decide.

Renaming a constant is now the breaking change that inserting one used to be: old rows hold the
old name. Normalising case before the lookup has a trap of its own:

```text
tr-TR: "shipped".toUpperCase() = SHİPPED
  No enum constant ValueOf.Status.SHİPPED
toUpperCase(Locale.ROOT) = SHIPPED
```

Use `toUpperCase(Locale.ROOT)`.

<!-- practice 3 -->

## A Constructor Runs Before the Enum's Static Fields Are Assigned

A currency enum that indexes itself by ISO code as each constant is built looks natural:

```java
EUR("978"), USD("840"), JPY("392");

private static final Map<String, Currency> BY_CODE = new HashMap<>();
private static final int MAX_CODES = 3;          // a constant variable: allowed

private final String code;

Currency(String code) {
    this.code = code;
    if (code.length() > MAX_CODES) throw new IllegalArgumentException(code);
    BY_CODE.put(code, this);
}
```

```text
a/Currency.java:15: error: illegal reference to static field from initializer
        BY_CODE.put(code, this);
        ^
```

`BY_CODE` is declared after the constants, as it must be, since the constants come first in an
enum's body. Static fields are assigned in the order they are declared, and the
constants are static fields too, so `EUR` is constructed while `BY_CODE` is still `null`. The
compiler refuses a constructor that refers to an enum's own static field, unless the field is a
constant variable: `MAX_CODES`, a `static final int` initialised with `3`, compiled without
complaint.

The compiler only checks direct references. Moving the `put` into a `private static void
register(Currency c)` and calling that from the constructor compiles, and fails when the class
first initialises:

```text
use 1: java.lang.ExceptionInInitializerError
  caused by java.lang.NullPointerException: Cannot invoke "java.util.Map.put(Object, Object)" because "Registered$Currency.BY_CODE" is null
use 2: java.lang.NoClassDefFoundError: Could not initialize class Registered$Currency
  caused by java.lang.ExceptionInInitializerError: Exception java.lang.NullPointerException [in thread "main"]
```

A class whose initialisation failed stays failed for the life of its class loader. The first
request gets the real cause; every later one gets `NoClassDefFoundError`, which reads like a
packaging problem, until a restart or a redeploy loads the class again. Build the index in a `static` block after the
constants, from `values()` (unit 7.2.3).

<!-- practice 4 -->

## Summary

- **An enum's type is closed.** It can't extend a class, declare a type parameter (JEP 301 was
  withdrawn) or be subclassed; enums share behaviour through interfaces.
- **A constant with a body is an anonymous subclass.** `getClass()` is then not the enum; use
  `getDeclaringClass()`. `switch`, `EnumSet` and `EnumMap` are unaffected.
- **An ordinal is a position.** A stored ordinal silently points at another constant after an
  insertion, deletion or reorder; store the name or a code of your own.
- **`valueOf` accepts only the exact name.** It throws `IllegalArgumentException` on an unknown
  name and `NullPointerException` on `null`; decide what an unknown name means where it enters.
- **A constructor runs before the enum's static fields are assigned.** A direct read is a compile
  error, an indirect one an `ExceptionInInitializerError` that leaves the class unusable.

## Reference

**What javac 21 says:**

| You write | Error |
|---|---|
| `enum Plan extends BaseRule { ... }` | `'{' expected` (a parse error: no `extends` clause) |
| `enum Key<T> { ... }` | `enums cannot be generic` |
| `class Critical extends Severity { }` | `cannot inherit from final Severity`, `enum classes are not extensible` |
| `final class Surcharge extends Fee { }` (`Fee` has a constant body) | `enum classes are not extensible` |
| `class MyEnum extends Enum<MyEnum>` | `classes cannot directly extend java.lang.Enum` |
| constructor reads a non-constant static field of its enum | `illegal reference to static field from initializer` |

**A constant with a body, `FLAT`, of `enum Fee` (Java 21):**

| Expression | Result |
|---|---|
| `FLAT.getClass()` | `Fee$1`, a `final` subclass of `Fee` |
| `FLAT.getDeclaringClass()` | `Fee` |
| `FLAT.getClass().isEnum()` | `false` |
| `FLAT.getClass().getSimpleName()` | `""` |
| `Fee.class.isSealed()` | `true` |
| `EnumSet.allOf((Class) FLAT.getClass())` (raw, as framework code holds it) | throws `ClassCastException` |
| `Enum.valueOf((Class) FLAT.getClass(), "PERCENT")` (raw) | throws `IllegalArgumentException` |
| `EnumSet.of(FLAT)`, `new EnumMap<>(Fee.class)`, `switch`, `instanceof Fee` | work as for any constant |

**Lookup by name:**

| Call | Result |
|---|---|
| `Status.valueOf("SHIPPED")` | `SHIPPED` |
| `Status.valueOf("shipped")`, `"SHIPPED "`, `""` | `IllegalArgumentException: No enum constant ...` |
| `Status.valueOf(null)` | `NullPointerException: Name is null` |

## Challenges

1. Your table has ten million rows of `ordinal()` values, and release 2 needs a constant in the
   middle. Plan the migration so that release 1 and release 2 can run side by side during the
   rollout. What do you store while both are live?
2. A partner sends a status you don't know yet. Where do you choose between rejecting the
   message, mapping it to `UNKNOWN`, and keeping the raw text, and what does each choice cost a
   report written a year later?
3. Search a codebase you know for `getClass()` in `equals`, in serializers and in maps keyed by
   `Class`. Which of them could receive an enum constant, and what happens on the day someone adds
   a body to it?

## Design Note: Why the Instances Are Closed

Each limit protects one guarantee: the constants listed in the source are all the
instances there will ever be. That lets `==` replace `equals`, makes `switch` exhaustive, lets
`EnumSet` be one bit per constant, and makes an enum the simplest safe singleton (unit 7.2.5).
A subclass, a type argument per constant, or a constant added at run time would each break one of
those. The price is that an enum fits a set you control and change only by redeploying. When the
set comes from configuration or from other teams, or each member needs a type or fields of its
own, a sealed interface or a registry is the better shape, and you give up the guarantees above.

## Code Examples

- Test: [TraditionalEnumLimitationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/TraditionalEnumLimitationsTest.java)
- Source: [TraditionalEnumLimitations.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/TraditionalEnumLimitations.java)
