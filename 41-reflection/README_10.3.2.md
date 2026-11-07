# 10.3.2. Accessing Fields, Methods, and Constructors

A logistics service moves from Java 8 to 21. The first shipment it saves throws
`InaccessibleObjectException` from the audit logger, which writes every saved object field by
field. The test suite fails too: a helper that swaps the billing code's `static final Clock` for a
fixed one, by clearing the field's `FINAL` bit first, now gets `NoSuchFieldException`. Nobody
changed either helper.

Once you hold a `Field`, `Method` or `Constructor` (unit 10.3.1 finds them), `setAccessible(true)`
switches off the language's access check for `get`, `set`, `invoke` and `newInstance`. The
sections below are the places where it stops working. Every output is from Java 21; Java 25
printed the same for the four programs.

## setAccessible Opens Your Own Classes, and java.base Only One Granted Package at a Time

The audit logger, reduced to its walk:

```java
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Prints an object field by field, the way an audit log or a deep-copy helper walks it. */
public class Dump {
    static class Shipment {
        private final String id = "S-1042";
        private final List<String> parcels = new ArrayList<>(List.of("P-1", "P-2"));
        private final Instant created = Instant.parse("2026-03-01T10:00:00Z");
    }

    static String dump(Object o) throws IllegalAccessException {
        if (o == null || o instanceof String || o instanceof Number) return String.valueOf(o);
        StringBuilder sb = new StringBuilder(o.getClass().getSimpleName()).append('{');
        for (Field f : o.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers())) continue;
            f.setAccessible(true);
            sb.append(f.getName()).append('=').append(dump(f.get(o))).append(' ');
        }
        return sb.append('}').toString();
    }

    public static void main(String[] args) throws Exception {
        try {
            System.out.println(dump(new Shipment()));
        } catch (RuntimeException e) {
            System.out.println(e);
        }
    }
}
```

Pause before the output: which field stops the walk, and what does one `--add-opens` change?

```text
$ java Dump
java.lang.reflect.InaccessibleObjectException: Unable to make field transient java.lang.Object[] java.util.ArrayList.elementData accessible: module java.base does not "opens java.util" to unnamed module @5305068a
$ java --add-opens java.base/java.util=ALL-UNNAMED Dump
java.lang.reflect.InaccessibleObjectException: Unable to make field private final long java.time.Instant.seconds accessible: module java.base does not "opens java.time" to unnamed module @7ef20235
$ java --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.time=ALL-UNNAMED Dump
Shipment{id=S-1042 parcels=ArrayList{elementData=Object[]{} size=2 } created=Instant{seconds=1772359200 nanos=0 } }
```

Code on the class path sits in the unnamed module, and `setAccessible` succeeds on any of its
members. A class in a named module opens its private members only if its module `opens` the
package to the caller. `java.base` opens none of its packages (unit 2.2.1 shows it on
`String.value`, unit 7.1.4 on the Java 17 upgrade), so the walk fails at the first JDK object it
reaches. Each grant covers one package: opening `java.util` moved the failure to `java.time`. An
executable jar can carry the same grants in its manifest,
`Add-Opens: java.base/java.util java.base/java.time`. `java -jar dump.jar` printed the third line;
the same jar run with `-cp` failed at `java.util`, because the attribute is read only with `-jar`.

That third line is also why the grant is a poor fix. The dump now shows `elementData` and
`seconds`, the JDK's private layout, which any release may change, and the parcel IDs are missing:
an array has no declared fields, so the walk never prints the elements.

`InaccessibleObjectException` extends `RuntimeException`, so `dump`'s `throws
IllegalAccessException` and a caller's `catch (ReflectiveOperationException e)` both miss it, and
the save fails. A walker that must survive classes it can't open asks first. `trySetAccessible()`
returns `false` where `setAccessible` would throw, and in place of the `setAccessible` line the
walker returns the object's public `toString()`:

```java
if (!f.trySetAccessible()) return o.toString();   // a package nobody opened
```

```text
$ java DumpFallback
Shipment{id=S-1042 parcels=[P-1, P-2] created=2026-03-01T10:00:00Z }
```

<!-- practice 1 -->

## A Final Instance Field Can Be Written, and Code Compiled Against Its Constant Never Sees It

A retry policy has one final field initialised with a constant and one assigned in the
constructor:

```java
final class RetryPolicy {
    private final int maxAttempts = 3;          // a constant expression
    private final long backoffMs;               // assigned in the constructor
    RetryPolicy(long backoffMs) { this.backoffMs = backoffMs; }
    int maxAttempts() { return maxAttempts; }
    long backoffMs() { return backoffMs; }
}
```

```java
RetryPolicy policy = new RetryPolicy(200);
Field attempts = RetryPolicy.class.getDeclaredField("maxAttempts");
Field backoff = RetryPolicy.class.getDeclaredField("backoffMs");
attempts.setAccessible(true);
backoff.setAccessible(true);
attempts.set(policy, 5);
backoff.set(policy, 800L);
System.out.println("maxAttempts: field " + attempts.get(policy) + ", getter " + policy.maxAttempts());
System.out.println("backoffMs:   field " + backoff.get(policy) + ", getter " + policy.backoffMs());
```

```text
maxAttempts: field 5, getter 3
backoffMs:   field 800, getter 800
```

Both writes succeeded. The getter still returns 3 because it never reads the field. A `final` field initialised with a constant expression is a constant variable,
and javac compiles its value into the code that reads it. `javap -c` shows it:

```text
  int maxAttempts();
    Code:
       0: iconst_3
       1: ireturn
  long backoffMs();
    Code:
       0: aload_0
       1: getfield      #13                 // Field backoffMs:J
       4: lreturn
```

The constructor-assigned field changed for every reader here, but nothing promises that. The
`Field.set` javadoc calls writing a final field meaningful only for blank finals (`backoffMs`, not
`maxAttempts`) during deserialization or reconstruction, before other code can see the object.
Anywhere else, it warns, other parts of the program may go on using the original value. A
reflective write to a `final` field belongs in code that builds the object, not in code that
changes one already in use.

## A Static Final Field and a Record Component Refuse the Write

The billing test from the opening swaps a `static final Clock`; a second test edits a record:

```java
import java.time.Clock;

final class Billing {
    static final Clock CLOCK = Clock.systemUTC();
}

record Money(long cents, String currency) {}
```

```java
Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
Field clock = Billing.class.getDeclaredField("CLOCK");
clock.setAccessible(true);
try {
    clock.set(null, fixed);
} catch (IllegalAccessException e) {
    System.out.println(e);
}
try {
    Field.class.getDeclaredField("modifiers");
} catch (NoSuchFieldException e) {
    System.out.println(e);
}

Money price = new Money(1999, "EUR");
Field cents = Money.class.getDeclaredField("cents");
cents.setAccessible(true);
try {
    cents.set(price, 1L);
} catch (IllegalAccessException e) {
    System.out.println(e);
}
System.out.println(price);
```

```text
java.lang.IllegalAccessException: Can not set static final java.time.Clock field Billing.CLOCK to java.time.Clock$FixedClock
java.lang.NoSuchFieldException: modifiers
java.lang.IllegalAccessException: Can not set final long field Money.cents to java.lang.Long
Money[cents=1999, currency=EUR]
```

`setAccessible(true)` succeeded on both fields, and `set` refused anyway. The `Field.set` javadoc
grants write access to a `final` field only when it is non-static and its class is neither a
record nor a hidden class. Unit 9.1.1 met the same refusal resetting a singleton's `INSTANCE`.

That refusal is older than modules, which is why the opening's helper first cleared the `FINAL`
bit through the private `modifiers` field of `Field` itself. On Java 21 reflection doesn't list
that field, so the trick ends in `NoSuchFieldException`. A test that needs a fixed clock passes it
in, through a constructor or a parameter, and the production code reads the injected `Clock`.

Reflection can't change a record's fields either, so a deserializer or a copy helper builds a
record through its canonical constructor. That constructor runs the record's
validation, which a field write would skip.

<!-- practice 2 -->

## Method.invoke Wraps the Method's Exceptions and Throws Its Own Argument Errors Unwrapped

An admin endpoint calls handler methods by name. It answers 400 to bad input and 500 to anything
else. `refund` rejects a non-positive amount with `IllegalArgumentException`:

```java
class Admin {
    public String refund(String orderId, int cents) {
        if (cents <= 0) throw new IllegalArgumentException("refund must be positive: " + cents);
        return "refunded " + cents + " on " + orderId;
    }
}
```

The endpoint finds the public method by name and calls it:

```java
static String naive(String name, Object... args) {
    try {
        return "200 " + find(name).invoke(new Admin(), args);
    } catch (IllegalArgumentException e) {
        return "400 " + e.getMessage();
    } catch (Exception e) {
        return "500 " + e;
    }
}
```

Two requests: an amount of `-500`, and an amount sent as the string `"500"`. Which status does each
get?

```text
naive:      500 java.lang.reflect.InvocationTargetException
naive:      400 argument type mismatch
```

Both answers are wrong. `invoke` never lets the method's exception through: it wraps whatever the
method throws, checked or unchecked, in `InvocationTargetException`, a checked
`ReflectiveOperationException`, with the original as `getCause()`. So the refund that `refund`
rejected became a 500 with no message. The string never reached `refund`: `invoke` threw that `IllegalArgumentException` itself, because the argument
didn't fit the `int` parameter, and it doesn't wrap its own exceptions. The first case reports a
client's mistake as a server error, and every 500 pages the on-call engineer. The second blames the
input in words that name no parameter.

The wrapper is what tells you whose exception it is:

```java
} catch (InvocationTargetException e) {
    Throwable cause = e.getCause();              // what refund itself threw
    return cause instanceof IllegalArgumentException
            ? "400 " + cause.getMessage() : "500 " + cause;
} catch (IllegalArgumentException e) {           // invoke refused the arguments
    return "400 bad arguments for " + name + ": " + e.getMessage();
}
```

With a third request, a correct one for order `O-77`:

```text
unwrapping: 400 refund must be positive: -500
unwrapping: 400 bad arguments for refund: argument type mismatch
unwrapping: 200 refunded 500 on O-77
```

`Constructor.newInstance` wraps the same way. Unit 9.2.3 unwraps inside a proxy's handler, where a
leaked wrapper turns into `UndeclaredThrowableException`.

<!-- practice 3 -->

<!-- practice 4 -->

## Summary

- **`setAccessible` opens class-path code; a `java.base` package opens only by `--add-opens` or
  `Add-Opens`, one package at a time.** `InaccessibleObjectException` is unchecked, and `trySetAccessible()` lets a walker
  fall back to the public API instead.
- **A `final` instance field can be written, and code compiled against its constant never sees
  it.** javac inlines constant values, and the javadoc warns that other readers may keep the old
  value.
- **A `static final` field and a record component refuse the write after `setAccessible`.** The
  `modifiers` trick is gone; inject the value, or call the canonical constructor.
- **`Method.invoke` wraps what the method throws in `InvocationTargetException` and throws its own
  `IllegalArgumentException` unwrapped.** Decide by the wrapper, then read `getCause()`.

## Reference

**Reading, writing and calling (Java 21, `java.lang.reflect` javadoc and this page's runs):**

| Call | Notes |
|---|---|
| `getDeclaredField(name)` | this class only, any access level; a superclass's field needs `getSuperclass()` |
| `field.get(obj)` / `field.set(obj, v)` | `obj` is `null` for a static field; primitives are boxed and unboxed |
| `method.invoke(obj, args...)` | `obj` is `null` for a static method; returns `null` for a `void` method |
| `getDeclaredMethod("add", Integer.class, Integer.class)` | `NoSuchMethodException` for `add(int, int)`: parameter types match exactly |
| a private member of a nested class, used from another class of the same top-level class | accessible without `setAccessible` (nestmates, Java 11+) |

**What `setAccessible(true)` allows:**

| Target | Result |
|---|---|
| any member of a class on the class path | succeeds |
| a non-public member of a `java.base` class (`ArrayList.elementData`, `Object.clone`) | `InaccessibleObjectException`; `trySetAccessible()` returns `false` |
| the same, with `--add-opens <module>/<package>=ALL-UNNAMED`, or `Add-Opens` in the manifest of a jar run with `-jar` | succeeds for that package only |
| `sun.misc.Unsafe.theUnsafe` (module `jdk.unsupported`, which opens `sun.misc`) | succeeds without flags |
| a member of a class in a named module | succeeds if the module `opens` the package to the caller (javadoc) |

**Writing a `final` field after `setAccessible(true)`:**

| Field | `Field.set` |
|---|---|
| instance field of an ordinary class | writes it; code compiled against a constant initialiser keeps the old value |
| `static final` field | `IllegalAccessException` |
| field of a record | `IllegalAccessException` |
| field of a hidden class | `IllegalAccessException` (javadoc) |

**Exceptions:**

| Thrown by | When | Type |
|---|---|---|
| `invoke`, `newInstance` | the method or constructor threw | `InvocationTargetException`, cause = the original |
| `invoke` | wrong argument count or type, `null` for a primitive parameter | `IllegalArgumentException`, unwrapped |
| `get`, `set`, `invoke` | the access check fails | `IllegalAccessException` |
| `setAccessible` | the package isn't opened to the caller | `InaccessibleObjectException`, unchecked |

## Challenges

1. The audit logger must keep printing your own classes field by field on Java 21. List the JDK
   types your entities hold, and decide for each between `--add-opens`, `toString()` and a
   formatter of your own. What does each choice cost at the next JDK upgrade?
2. A library you can't change calls `setAccessible` on JDK internals. How do you find out which
   packages it needs without reading its source, and how do you notice when the list grows?
3. A dispatcher catches `InvocationTargetException` and rethrows `getCause()`. What does its caller
   lose compared with the version on this page, and when is rethrowing the right choice?

## Design Note: Why the JDK Closed Its Packages

Until Java 9, `setAccessible(true)` reached everything, and serialization and mocking libraries
came to depend on the JDK's private fields. Each such dependency froze a piece of the JDK's
internals, because changing a private field broke code that was never meant to see it. Modules let
`java.base` say which packages are API, and strong encapsulation, the default since Java 16 and the
only mode since 17, makes every exception visible at launch: an `--add-opens` or an `Add-Opens`
entry names the package and the module that gets it.

The cost falls on generic tools. A serializer or a deep-copy helper now needs either a grant per
package or a fallback to the public API, which loses whatever that API doesn't show. The `final`
rules make the same trade: a `static final` field or a record component that core reflection
can't change is one the rest of the program can rely on.

## Code Examples

- Test: [FieldMethodConstructorAccessTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/reflection/FieldMethodConstructorAccessTest.java)
- Source: [FieldMethodConstructorAccess.java](src/main/java/com/github/msorkhpar/claudejavatutor/reflection/FieldMethodConstructorAccess.java)
