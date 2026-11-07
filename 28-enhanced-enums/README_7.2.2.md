# 7.2.2. Declaring Generic Enums

A service keeps its settings in `enum Setting { PORT, HOST, TIMEOUT }` and reads them through
`Object get(Setting)`, so every caller casts. A refactor changes `TIMEOUT` from a number of seconds
to a `Duration`. Everything compiles, and the first request after the deploy fails in code nobody
touched:

```text
Exception in thread "main" java.lang.ClassCastException: class java.time.Duration cannot be cast to class java.lang.Integer (java.time.Duration and java.lang.Integer are in module java.base of loader 'bootstrap')
	at Open.main(Open.java:14)
```

The fix everyone reaches for is `enum Setting<T>`, so that `get(TIMEOUT)` returns a `Duration`
and the compiler finds the stale cast. Despite this unit's title, Java has no generic enums.

## An Enum Can't Declare a Type Parameter; JEP 301 Proposed It and Was Withdrawn

```java
enum Setting<T> {
    PORT, HOST
}
```

```text
Setting.java:1: error: enums cannot be generic
enum Setting<T> {
             ^
1 error
```

That is javac 21; javac 25 prints the same. JEP 301, "Enhanced Enums", proposed type parameters on
enums, so that each constant could carry its own type argument (`PORT` a `Setting<Integer>`,
`HOST` a `Setting<String>`). It was withdrawn and never shipped.

Erasure is not the obstacle: a generic class can hold `static final` constants with different
type arguments, which is the first replacement below. The enum declaration has no place for a type
parameter, so every constant has the one type `Setting`, and nothing ties a constant to the type of
its value. Unit 7.2.1 shows the usual result, an `Object` field and a cast at every call site. An
enum may declare generic methods (unit 7.2.4), but those don't give a constant its own type either.

## A Final Class With Typed Constants Lets the Compiler Check Every Key

A class with a private constructor and `public static final` instances behaves like an enum's
constant list. Make the class generic and each constant gets its own type argument:

```java
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class Key<T> implements Serializable {
    private static final List<Key<?>> ALL = new ArrayList<>();

    public static final Key<Integer> PORT = new Key<>("port", Integer.class, 8080);
    public static final Key<String> HOST = new Key<>("host", String.class, "127.0.0.1");
    public static final Key<Duration> TIMEOUT = new Key<>("timeout", Duration.class, Duration.ofSeconds(5));

    private final String name;
    private final Class<T> type;
    private final T defaultValue;

    private Key(String name, Class<T> type, T defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        ALL.add(this);
    }

    public String name() { return name; }
    public Class<T> type() { return type; }
    public T defaultValue() { return defaultValue; }
    public static List<Key<?>> values() { return List.copyOf(ALL); }
    @Override public String toString() { return name; }
}
```

The store's methods take the key's `T`, so the value's type follows the key:

```java
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class Config implements Serializable {
    private final Map<Key<?>, Object> values = new HashMap<>();

    public <T> void set(Key<T> key, T value) {
        values.put(key, value);
    }

    public <T> T get(Key<T> key) {
        Object value = values.get(key);
        return value == null ? key.defaultValue() : key.type().cast(value);
    }
}
```

`int port = config.get(Key.PORT)` and `Duration timeout = config.get(Key.TIMEOUT)` need no cast.
Pause before reading on: do these two lines fail at run time, or earlier?

```java
config.set(Key.TIMEOUT, 30);
String port = config.get(Key.PORT);
```

```text
Wrong.java:3: error: method set in class Config cannot be applied to given types;
        config.set(Key.TIMEOUT, 30);
              ^
  required: Key<T>,T
  found:    Key<Duration>,int
  reason: inference variable T has incompatible bounds
    equality constraints: Duration
    lower bounds: Integer
  where T is a type-variable:
    T extends Object declared in method <T>set(Key<T>,T)
Wrong.java:4: error: incompatible types: inference variable T has incompatible bounds
        String port = config.get(Key.PORT);
                                ^
    equality constraints: Integer
    upper bounds: String,Object
  where T is a type-variable:
    T extends Object declared in method <T>get(Key<T>)
2 errors
```

Both fail to compile; the second is the opening's mistake, now caught by javac. Only `Config`
casts, through the `Class<T>` object, because its one map stores every value as `Object`. The
private constructor keeps the set closed: no code outside `Key` can construct a fourth key.

## The Class Must Rebuild values(), valueOf() and Serialization Itself

For an enum, the compiler writes `values()` and `valueOf()`, `switch` accepts its constants, and
serialization writes the constant's name and reads back the existing constant. A class gets none
of that.

### Spot the Problem: The Restored Config Runs on Defaults

A node caches its `Config` by serializing it, and reads it back after a restart. Port 9090 was
set; the restored config disagrees:

```java
Key<?> port = roundTrip(Key.PORT);                 // write to bytes, read back
System.out.println("copy == Key.PORT: " + (port == Key.PORT));
System.out.println("copy.equals(Key.PORT): " + port.equals(Key.PORT));
Config config = new Config();
config.set(Key.PORT, 9090);
Config restored = roundTrip(config);
System.out.println("restored port: " + restored.get(Key.PORT));
```

```text
copy == Key.PORT: false
copy.equals(Key.PORT): false
restored port: 8080
```

Deserialization creates a new object. The restored map's keys are copies of `PORT`, and `Key`
inherits identity `equals` from `Object`, so `get(Key.PORT)` misses and returns the default.
Nothing throws; the node listens on the wrong port.

Resolve by name, as enum serialization does. `readResolve` replaces the object the stream produced
with the one it returns:

```java
@Serial
private Object readResolve() throws ObjectStreamException {
    for (Key<?> key : ALL) {
        if (key.name.equals(name)) return key;
    }
    throw new InvalidObjectException("no key named " + name);
}
```

```text
copy == Key.PORT: true
copy.equals(Key.PORT): true
restored port: 9090
```

Overriding `equals` and `hashCode` by name would fix the map lookup but not `==`, which every
caller used while `Key` was an enum. Resolving fixes both.

### The Registry Is Yours, and So Is Its Order

`values()` reads the list each constructor appends to. Move `ALL` below the constants and the
class still compiles but can't load, because the constants' initializers run first, while `ALL` is
still `null`:

```text
Exception in thread "main" java.lang.ExceptionInInitializerError
	at Main.main(Main.java:3)
Caused by: java.lang.NullPointerException: Cannot invoke "java.util.List.add(Object)" because "Key.ALL" is null
	at Key.<init>(Key.java:14)
	at Key.<clinit>(Key.java:5)
	... 1 more
```

`values()` returns an unmodifiable copy, so no caller can change which keys exist (an enum's
`values()` returns a new array on every call for the same reason). Write `valueOf(String)` too, and
follow the enum: throw `IllegalArgumentException` for an unknown name rather than return `null`.

You can't rebuild `switch`. A case label is a constant expression, an enum constant or a pattern,
and `Key.PORT` is none of them, so javac refuses `case Key.PORT ->`. The nearest is a guard,
`case Key<?> k when k == Key.PORT ->`, and a switch of guards always needs a catch-all (`default`,
or an unguarded `case Key<?> k`), so the compiler never tells you a key was left out. `EnumSet` and
`EnumMap` require `E extends Enum<E>`; a `HashMap` takes their place.

<!-- practice 1 -->

## A Sealed Interface of Records Brings Back an Exhaustive switch, but Not Typed Cases

A sealed interface lists its implementations, and each record implements it with its own type
argument:

```java
import java.time.Duration;

public sealed interface Setting<T> {
    String key();
    T parse(String raw);

    record Port() implements Setting<Integer> {
        public String key() { return "port"; }
        public Integer parse(String raw) { return Integer.valueOf(raw); }
    }

    record Host() implements Setting<String> {
        public String key() { return "host"; }
        public String parse(String raw) { return raw.strip(); }
    }

    record Timeout() implements Setting<Duration> {
        public String key() { return "timeout"; }
        public Duration parse(String raw) { return Duration.parse(raw); }
    }
}
```

`Integer port = new Setting.Port().parse("9090")` is typed as before. A switch over the records
with no `default` is checked for coverage:

```java
static String describe(Setting<?> setting) {
    return switch (setting) {
        case Setting.Port p -> "listen port";
        case Setting.Host h -> "bind address";
        case Setting.Timeout t -> "request timeout";
    };
}
```

Add a fourth record, `Retries`, and this method stops compiling until someone decides what it
means:

```text
Describe.java:3: error: the switch expression does not cover all possible input values
        return switch (setting) {
               ^
1 error
```

A `default ->` branch added "for safety" turns that error off without a warning (even under
`-Xlint:all`), and `Retries` silently gets whatever the default says. Leave it out on sealed types.

A `case` doesn't narrow `T`. Inside `case Setting.Port p` the setting must be a
`Setting<Integer>`, so a generic parse through a switch looks as if it should work:

```java
static <T> T parse(Setting<T> setting, String raw) {
    return switch (setting) {
        case Setting.Port p -> Integer.valueOf(raw);
        case Setting.Host h -> raw.strip();
        case Setting.Timeout t -> Duration.parse(raw);
    };
}
```

```text
Parse.java:6: error: incompatible types: bad type in switch expression
            case Setting.Port p -> Integer.valueOf(raw);
                                                  ^
    Integer cannot be converted to T
  where T is a type-variable:
    T extends Object declared in method <T>parse(Setting<T>,String)
```

javac does not narrow `T` to `Integer` inside the case (two more errors follow for the other
cases). A `(T)` on each branch compiles with unchecked warnings, and then a body pasted into the
wrong case, `case Setting.Port p -> (T) raw.strip()`, fails where the opening's cast failed, at the
caller:

```text
Exception in thread "main" java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
	at Cast.main(Cast.java:14)
```

Put typed behaviour in a method each record implements, as `parse` is above, and keep the switch
for decisions whose result type every case shares.

Records are values, not singletons: `new Setting.Port() == new Setting.Port()` is `false` and
`equals` is `true`. Compare them with `equals` and key maps with `HashMap`. A serializable record
comes back as an `equals` copy, so it needs no `readResolve`. `values()` is still hand-written, and
nothing checks that list; `Setting.class.getPermittedSubclasses()` at least lists the record
classes without going stale.

<!-- practice 2 -->

## Summary

- **Java enums can't be generic.** javac says `enums cannot be generic`; JEP 301 proposed them and
  was withdrawn. Every constant has the enum's one type.
- **A final class with typed constants** (`Key<Integer> PORT`) makes a wrong value or a wrong
  target type a compile error, and its private constructor keeps the set closed.
- **That class rebuilds what the enum got for free.** Without `readResolve` a deserialized key is a
  copy that misses every lookup; the registry is declared before the constants; there is no
  `switch` on its constants and no `EnumMap`.
- **A sealed interface of records** gets the exhaustive `switch` back, but a `case` does not
  narrow `T`, so typed behaviour lives in each record's methods. Records compare by `equals`.

## Reference

**What each choice gives you (Java 21; each row ran in the programs behind this unit):**

| | `enum` | final class, typed constants | sealed interface, records |
|---|---|---|---|
| Own type argument per constant | no (`enums cannot be generic`) | yes | yes |
| Exhaustive `switch` expression | yes | no (`case Key.PORT` doesn't compile) | yes, over type patterns |
| `values()` / `valueOf()` | generated | hand-written registry | hand-written; `getPermittedSubclasses()` lists the classes |
| `EnumSet` / `EnumMap` | yes | no (bound `E extends Enum<E>`) | no |
| Compare with `==` | yes | yes, if `readResolve` resolves | no, use `equals` |
| After deserialization | the same constant | a new copy unless `readResolve` | if `Serializable`: a new copy, `equals` to the original |
| Set closed by | the language | the private constructor | `sealed` |

## Challenges

1. An enum may implement a generic interface: `enum IntOption implements Option<Integer>` next
   to `enum TextOption implements Option<String>`, both permitted by a sealed `Option<T>`. What
   does that give back (`EnumMap`, serialization by name), and what does it cost when a setting
   changes type?
2. A typed key is sent to another service as JSON. Which of the designs makes the receiving
   side's lookup simplest, and what replaces `readResolve` there?
3. Your team has 40 settings in an `Object`-valued enum. Plan a migration to typed keys that ships
   in steps, with the old and the new API side by side.

## Design Note: Why a Constant With Its Own Type Fights the Enum

An enum is one type with a fixed list of instances, and the platform relies on that: `EnumSet` is
a bit vector and `EnumMap` an array, both indexed by ordinal, a `switch` expression checks coverage
against the list, and serialization needs only a name. A constant with its own type argument splits
that one type into several, `Setting<Integer>` and `Setting<String>`, and each of those services
would have to say what it means for them. Java kept enums as they were and left typed keys to
classes and sealed hierarchies.

## Code Examples

- Test: [GenericEnumPatternsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumPatternsTest.java)
- Source: [GenericEnumPatterns.java](src/main/java/com/github/msorkhpar/claudejavatutor/enhancedenums/GenericEnumPatterns.java)
