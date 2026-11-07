# 7.2.4. Generic Methods in Enums

The payments service reads its settings through an enum, and `RetryPolicy` takes its limit from
it when the service starts:

```java
import java.util.Map;
import java.util.Optional;

final class RetryPolicy {
    private final Optional<Integer> maxRetries;

    RetryPolicy(Map<String, String> props) {
        maxRetries = Setting.RETRIES.find(props);
    }

    int attemptsLeft(int used) {
        return maxRetries.orElse(3) - used;
    }
}
```

The service starts and passes its health check. The first payment that needs a retry fails:

```text
Exception in thread "main" java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
	at RetryPolicy.attemptsLeft(RetryPolicy.java:12)
	at Opening.main(Opening.java:8)
```

`RetryPolicy` hasn't changed in a year. The bug is in `Setting.find`, which `RetryPolicy` calls, a
generic method on an enum, and the stack trace doesn't mention it.

## An Enum Can't Take a Type Parameter, but Its Methods Can, and Each Call Picks T

An enum declaration can't be generic:

```text
Setting.java:1: error: enums cannot be generic
enum Setting<T> {
             ^
```

JEP 301 proposed generic enums and was withdrawn; unit 7.2.2 covers the replacements. A method of
an enum can still declare its own type parameter. This is the `Setting` that `RetryPolicy` uses:

```java
import java.util.Map;
import java.util.Optional;

enum Setting {
    HOST("db.host"), RETRIES("http.retries");

    private final String key;

    Setting(String key) { this.key = key; }

    <T> T read(Map<String, String> props) {
        return (T) props.get(key);
    }

    <T> Optional<T> find(Map<String, String> props) {
        return Optional.ofNullable((T) props.get(key));
    }
}
```

With `props` holding `http.retries=3`, which of these lines compile?

```java
Integer retries = Setting.RETRIES.read(props);
String text = Setting.RETRIES.read(props);
```

Both do. `T` belongs to each call, not to the constant: the compiler infers it from what the
caller assigns the result to, so `T` is `Integer` in the first line and `String` in the second.
Nothing ties `RETRIES` to `Integer`, because a constant has no type argument to carry it; only a
typed key (unit 7.2.2) can make that promise.

## An Unchecked Cast Checks Nothing Where It Is Written

javac compiles `Setting` with a note to recompile with `-Xlint:unchecked`, which gives:

```text
Setting.java:12: warning: [unchecked] unchecked cast
        return (T) props.get(key);
                            ^
  required: T
  found:    String
  where T is a type-variable:
    T extends Object declared in method <T>read(Map<String,String>)
```

After erasure, `T` inside `read` is `Object`, so `(T)` compiles to no check at all. The compiler
adds a check (a `checkcast` instruction) in the caller, at the point where the value meets a
concrete type, and only when the target needs it: even an explicit `<Integer>` adds no check when
the result goes into an `Object`. Where the check lands decides where, or whether, the program
fails (`r` is the result of `RETRIES.find(props)`):

```text
Integer retries = RETRIES.read(props)      -> ClassCastException in CallSites.main
String text = RETRIES.read(props)          -> 3 (String)
Object o = RETRIES.<Integer>read(props)    -> 3 (String)
Optional<Integer> r = RETRIES.find(props)  -> Optional[3]
r.isPresent()                              -> true
r.orElse(3) - 1                            -> ClassCastException in CallSites.main
r.map(n -> n * 2)                          -> ClassCastException in Optional.map
r.equals(Optional.of(3))                   -> false
```

An `Optional<Integer>` holding a `String` is stored without complaint, which is how `RetryPolicy`
started cleanly. It fails in the caller's code at `orElse` and inside the JDK at `map`. At
`equals`, which takes an `Object`, no check runs at all: it answers `false` and sends a comparison
down the wrong branch. None of these places is the enum, so the developer reading the trace starts
in the wrong class.

The warning is the one message that points at the cause. Putting `@SuppressWarnings("unchecked")`
on such a method removes it and leaves the bug.

## A `Class<T>` Token Makes the Cast Real, Inside the Method

The method can't see `T`, but the caller can hand it over as a `Class<T>` object, a **type
token**. `Class.cast` checks at run time, so the check moves into the enum:

```java
import java.util.Map;
import java.util.function.Function;

enum Setting {
    HOST("db.host", raw -> raw),
    RETRIES("http.retries", Integer::valueOf);

    private final String key;
    private final Function<String, ?> parser;

    Setting(String key, Function<String, ?> parser) {
        this.key = key;
        this.parser = parser;
    }

    <T> T read(Map<String, String> props, Class<T> type) {
        return type.cast(parser.apply(props.get(key)));
    }
}
```

It compiles with no warning under `-Xlint:unchecked`. Each constant now parses its own value, and
the token decides whether the caller may have it:

```text
RETRIES.read(props, Integer.class)     -> 3 (Integer)
RETRIES.read(props, Number.class)      -> 3 (Integer)
RETRIES.read(props, String.class)      -> ClassCastException: Cannot cast java.lang.Integer to java.lang.String
                                          in Class.cast, called from Setting.read
RETRIES.read(props, int.class)         -> ClassCastException: Cannot cast java.lang.Integer to int
                                          in Class.cast, called from Setting.read
```

A wrong request now fails on its own line, with both types in the message. `cast` accepts any
instance of the token's class, so a supertype such as `Number` works.

`int.class` has the type `Class<Integer>` and so compiles, but no object is
an instance of a primitive type, and `int.class.isInstance(3)` is `false`; a method that should accept
`int.class` must map it to `Integer.class` first. And a token names a class, not a parameterized type:
`List<Integer>.class` doesn't compile, so `Class.cast` can check that a value is a `List` but not
what the list holds. A key missing from `props` is no longer an empty `Optional`, either:
`HOST.read` returns `null`, since `cast(null)` passes, and `RETRIES.read` throws
`NumberFormatException: Cannot parse null string` from its parser.

<!-- practice 1 -->

## `E extends Enum<E>` Accepts Any Enum and Hands Back That Enum's Own Type

The JDK's enum utilities all declare the same bound:

```java
public static <T extends Enum<T>> T valueOf(Class<T> enumClass, String name)   // Enum
public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType)       // EnumSet
public EnumMap(Class<K> keyType)                  // EnumMap<K extends Enum<K>, V>
```

`Enum` itself is declared `Enum<E extends Enum<E>>`, and the compiler makes an enum
`Priority { LOW, NORMAL, HIGH }` extend `Enum<Priority>`. So `E extends Enum<E>` means "an enum type `E`", and a call with
any other class doesn't compile:

```text
Bounds.java:7: error: method valueOf in class Enum<E> cannot be applied to given types;
        Enum.valueOf(String.class, "LOW");
            ^
  required: Class<T>,String
  found:    Class<String>,String
  reason: inference variable T has incompatible bounds
    equality constraints: String
    upper bounds: Enum<T>
  where T,E are type-variables:
    T extends Enum<T> declared in method <T>valueOf(Class<T>,String)
    E extends Enum<E> declared in class Enum
```

The bound also lets `Enum`'s own methods speak in `E`: `compareTo(E)` accepts only constants of the
same enum, and `getDeclaringClass()` returns `Class<E>`. A method that moves a status to the next
constant uses that:

```java
final class Cycle {

    static <E extends Enum<E>> E next(E current) {
        E[] all = current.getDeclaringClass().getEnumConstants();
        return all[(current.ordinal() + 1) % all.length];
    }
}
```

### Spot the Problem: `getClass()` in Place of `getDeclaringClass()`

A colleague writes the same method with `getClass()`. The compiler refuses `E[] all =
current.getClass().getEnumConstants()` (`CAP#1[] cannot be converted to E[]`), so they add a cast:

```java
@SuppressWarnings("unchecked")
static <E extends Enum<E>> E next(E current) {
    E[] all = (E[]) current.getClass().getEnumConstants();
    return all[(current.ordinal() + 1) % all.length];
}
```

The enum has a constant with a body:

```java
enum Shipment {
    CREATED,
    IN_TRANSIT {
        @Override boolean moving() { return true; }
    },
    DELIVERED;

    boolean moving() { return false; }
}
```

```text
CycleByClass.next(CREATED)                 -> IN_TRANSIT
CycleByClass.next(IN_TRANSIT)              -> java.lang.NullPointerException: Cannot read the array length because "all" is null
IN_TRANSIT.getClass()                      -> class Shipment$1
IN_TRANSIT.getClass().getEnumConstants()   -> null
IN_TRANSIT.getDeclaringClass()             -> class Shipment
```

`IN_TRANSIT` is an instance of an anonymous subclass (unit 7.2.1), and `getEnumConstants()` returns
`null` for a class that isn't an enum. The types had said so: `getClass()` returns `Class<? extends
Enum>`, which promises nothing about `E`, and the cast hid the compile error that said so. With `getDeclaringClass()`, `next(IN_TRANSIT)` is `DELIVERED`, and `next(DELIVERED)` wraps
to `CREATED`.

## A Generic `parse` Reads Any Enum Through `getEnumConstants()`

An import reads shipment states and priorities from CSV files, written by people: `in-transit`,
` High `. `Enum.valueOf` matches the exact name only:

```text
Enum.valueOf(Shipment.class, "in-transit") -> java.lang.IllegalArgumentException: No enum constant Shipment.in-transit
```

One generic method can do the lenient match for every enum, because `Class<E>` hands over the
constants as an `E[]`:

```java
import java.util.EnumSet;
import java.util.Set;

final class Parse {

    static <E extends Enum<E>> E parse(Class<E> type, String text) {
        String wanted = text.trim().replace('-', '_').replace(' ', '_');
        for (E constant : type.getEnumConstants()) {
            if (constant.name().equalsIgnoreCase(wanted)) return constant;
        }
        throw new IllegalArgumentException(type.getSimpleName() + " has no constant for '" + text + "'");
    }

    static <E extends Enum<E>> Set<E> parseAll(Class<E> type, String csv) {
        Set<E> result = EnumSet.noneOf(type);
        for (String part : csv.split(",")) result.add(parse(type, part));
        return result;
    }
}
```

```text
parse(Shipment.class, "in-transit")        -> IN_TRANSIT
parse(Priority.class, " High ")            -> HIGH
parse(Priority.class, "urgent")            -> java.lang.IllegalArgumentException: Priority has no constant for 'urgent'
parseAll(Shipment.class, "delivered,created") -> [CREATED, DELIVERED]
```

The caller gets a `Shipment` back, with no cast. `EnumSet.noneOf(type)` needs the same `Class<E>`
and keeps the constants in declaration order, whatever order the file listed them in.

`getEnumConstants()`, like `values()`, returns a new copy of the array on every call. Calling it for every row of a large
file allocates an array per row. A hot path builds a lookup map once per enum (unit 7.2.3).

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **An enum can't take a type parameter, but its methods can, and each call picks `T`.** The
  constant can't promise a type; any caller can ask `RETRIES` for a `String`.
- **An unchecked cast checks nothing where it is written.** The value is checked where it meets a
  concrete type in the caller, or never, so the trace points away from the enum.
- **A `Class<T>` token makes the cast real, inside the method.** It fails at the wrong request, but
  can't express `int` (map it to `Integer`) or `List<Integer>`.
- **`E extends Enum<E>` accepts any enum and hands back that enum's own type.** Use
  `getDeclaringClass()`, which returns `Class<E>`; `getClass()` of a constant with a body isn't an
  enum class.
- **A generic `parse` reads any enum through `getEnumConstants()`.** It returns the caller's own
  type and pairs with `EnumSet.noneOf(type)`.

## Reference

| Call (Java 21) | Result |
|---|---|
| `enum E<T> {}` | compile error: `enums cannot be generic` |
| `(T) value` in a generic method | `unchecked cast` warning under `-Xlint:unchecked`; no check at run time |
| `type.cast(value)` | returns `value`, or throws `ClassCastException` naming both types; `null` passes |
| `int.class` | type `Class<Integer>`; `isInstance` is `false` for every object |
| `Enum.valueOf(type, name)` | exact name only; `IllegalArgumentException` otherwise, `NullPointerException` for a null name |
| `constant.getDeclaringClass()` | the enum class, `Class<E>`, for every constant |
| `constant.getClass()` | the enum class, or an anonymous subclass for a constant with a body |
| `Class.getEnumConstants()` | a new array per call; `null` for a class that isn't an enum |
| `EnumSet.noneOf(type)`, `new EnumMap<>(type)` | need the `Class<E>`; iterate in declaration order |

## Challenges

1. The settings enum grows to forty keys of six types. Compare the `Class<T>` token with a final
   class of typed keys (unit 7.2.2): what does each catch at compile time, and what does each cost
   when a key is added?
2. A library method takes `Class<T>` and you need it to return a `List<Order>`. What can you pass,
   and where does the unchecked cast end up?
3. Find the `@SuppressWarnings("unchecked")` annotations in a codebase you know. For each, which
   line would the `ClassCastException` point at if the cast were wrong?

## Design Note: Why Enum Is Declared `Enum<E extends Enum<E>>`

The self-referencing bound lets methods written once in `Enum` talk about the subclass:
`Priority.LOW.compareTo(Shipment.CREATED)` doesn't compile (`Shipment cannot be converted to
Priority`), and `EnumSet` and `EnumMap` get the constants of exactly the caller's enum with no cast
in the caller's code.

The price is the signatures. `<E extends Enum<E>>` is hard to read, and a reader meeting it in
`EnumSet` for the first time has to learn that it only means "an enum". It says nothing about any
other type: a setting's value type still needs a token, and a token still can't name
`List<Integer>`.

## Code Examples

- Implementation: [GenericEnumMethods.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumMethods.java)
- Tests: [GenericEnumMethodsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumMethodsTest.java)
