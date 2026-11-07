# 4.3.2. Types of Method References (Static, Instance, Constructor)

A weather service groups each night's readings by whole degree:

```java
for (Reading r : night) byDegree.computeIfAbsent(r.celsius(), ArrayList::new).add(r);
```

It ran all summer, until the first night below zero, when every batch failed with
`IllegalArgumentException: Illegal Capacity: -1`, thrown from a line that never mentions a
capacity. `ArrayList::new` has no parameter list: the interface it is passed to chose the
constructor, and passed it the temperature.

A method reference comes in four kinds: static (`Integer::parseInt`), bound (`prefix::concat`),
unbound (`String::length`) and constructor (`ArrayList::new`). Each maps the functional
interface's parameters onto a call in its own way. Unit 4.3.1 covers what all four share.

## `Class::method` Is Static or Unbound, and the Method's Declaration Decides Which

`Integer::parseInt` and `String::length` look alike, and both fit a `Function<String, Integer>`:

```java
Function<String, Integer> parse = Integer::parseInt;      // Integer.parseInt(s)
Function<String, Integer> length = String::length;        // s.length()
Comparator<String> byName = String::compareToIgnoreCase;  // a.compareToIgnoreCase(b)
System.out.println(parse.apply("0042") + " " + length.apply("0042"));
List<String> names = new ArrayList<>(List.of("bob", "Alice", "carol"));
names.sort(byName);
System.out.println(names);
```

```text
42 4
[Alice, bob, carol]
```

`parseInt` is static, so the argument goes straight to it. `length` is an instance method with
no parameters, so the argument becomes the receiver. With more parameters, the first becomes the
receiver and the rest the arguments: as a `Comparator<String>`, `compare(a, b)` calls
`a.compareToIgnoreCase(b)`. An unbound reference to a fixed-arity method needs one more interface
parameter than the method has.

Nothing in the text `Class::method` says which of the two it is. The compiler searches both ways:
for a static method that takes all the interface's parameters, and for an instance method that
takes all but the first. When both searches find one, it does not pick:

```java
import java.util.function.Function;

public class Ambiguous {
    Function<Integer, String> label = Integer::toString;
}
```

```text
Ambiguous.java:4: error: incompatible types: invalid method reference
    Function<Integer, String> label = Integer::toString;
                                      ^
    reference to toString is ambiguous
      both method toString(int) in Integer and method toString() in Integer match
1 error
```

`Integer` has a static `toString(int)` that takes the `Integer` by unboxing, and the instance
`toString()` that takes it as the receiver. `Long::toString` and `Integer::hashCode` fail the same
way. `Object::toString` and `String::valueOf` compile, because each leaves only one search with
a match, and so does the lambda `i -> Integer.toString(i)`, an ordinary call with one argument.
Unit 4.3.3 covers how overloads of one kind are chosen.

### Through a Supertype, the Call Still Reaches the Override

The class before `::` in an unbound reference can be a superclass or an interface of the receiver.
With `record Sku(String code, int qty) {}`:

```java
Function<Object, String> show = Object::toString;
ToIntFunction<CharSequence> chars = CharSequence::length;
System.out.println(show.apply(new Sku("A-7", 3)));
System.out.println(chars.applyAsInt("abc") + " " + chars.applyAsInt(new StringBuilder("abcdef")));
```

```text
Sku[code=A-7, qty=3]
3 6
```

`Object::toString` printed the record's own `toString`, not `Sku@` and a hash. The class before
`::` decides what compiles and what the interface's first parameter may be; the receiver's class
decides which body runs, as for `s.toString()`.

<!-- practice 3 -->

## A Bound Reference Holds Its Receiver, and the Interface's Arguments Become the Method's

In `expr::method`, the expression supplies the receiver, so every parameter of the interface goes
to the method. An invoice service tags ids with a prefix, then adds a region suffix the same way:

```java
String prefix = "INV-";
Function<String, String> tag = prefix::concat;             // prefix.concat(s)
System.out.println(tag.apply("1042"));
String suffix = "-EU";
Function<String, String> region = suffix::concat;          // suffix.concat(s), not s.concat(suffix)
System.out.println(region.apply("1042"));
```

```text
INV-1042
-EU1042
```

The second reference puts the suffix in front. A bound reference cannot make
the incoming value the receiver: the receiver was fixed when the reference was created. When the
value must be the receiver and something else the argument, write the lambda
`s -> s.concat(suffix)`. The same trap turns `term::contains` into "does the term contain this
line?" when the filter meant "does this line contain the term?".

### `this::` Dispatches; `super::` Is Fixed to the Parent's Body

In an instance method or constructor, `this` and `super` can stand before `::`. Both bind the current object, but they
call different bodies:

```java
import java.util.function.IntFunction;

class PriceFormat {
    String format(int cents) {
        return cents / 100 + "." + String.format("%02d", cents % 100);
    }
}

class EuroFormat extends PriceFormat {
    @Override
    String format(int cents) {
        return super.format(cents) + " EUR";
    }

    IntFunction<String> parent() { return super::format; }

    IntFunction<String> own() { return this::format; }
}
```

On a `new EuroFormat()`, `parent().apply(1250)` and `own().apply(1250)` give:

```text
12.50 | 12.50 EUR
```

`this::format` behaves like `this.format(c)`: it runs the override of whatever class the object
is, including a subclass written later. `super::format` behaves like `super.format(c)`: it always
runs `PriceFormat`'s body. It skips dynamic dispatch, as `Iface.super::m` does for an interface's
default method, and it is how you pass the parent's version as a callback.

<!-- practice 1 -->

## `Class::new` Picks Its Constructor From the Interface's Parameters

A constructor reference has no parameter list of its own. The interface's parameters are the
constructor's arguments, so the target type chooses the constructor:

```java
Supplier<List<String>> empty = ArrayList::new;             // new ArrayList<>()
Function<Integer, List<String>> sized = ArrayList::new;    // new ArrayList<>(int initialCapacity)
System.out.println(empty.get() + " " + sized.apply(5) + " size=" + sized.apply(5).size());
```

```text
[] [] size=0
```

`sized.apply(5)` is an empty list with room for five elements, not a list holding `5`. In the
opening loop, with `record Reading(String sensor, int celsius)`, `computeIfAbsent` takes a `Function<? super K, ? extends V>` and calls it with
the key. With `Integer` keys, `ArrayList::new` resolves to `ArrayList(int initialCapacity)`, and the
temperature becomes the capacity:

```java
Map<Integer, List<Reading>> byDegree = new TreeMap<>();
List<Reading> night = List.of(new Reading("roof", 3), new Reading("yard", 1), new Reading("roof", -1));
try {
    for (Reading r : night) byDegree.computeIfAbsent(r.celsius(), ArrayList::new).add(r);
} catch (IllegalArgumentException e) {
    System.out.println(e);
}
Map<Integer, List<Reading>> fixed = new TreeMap<>();
for (Reading r : night) fixed.computeIfAbsent(r.celsius(), k -> new ArrayList<>()).add(r);
System.out.println(fixed.keySet());
```

```text
java.lang.IllegalArgumentException: Illegal Capacity: -1
[-1, 1, 3]
```

Capacities 3 and 1 are legal; `ArrayList(int)` throws `IllegalArgumentException` for a negative
one, so the first negative reading threw. Large positive keys fail another way: a map keyed by
customer id reserves an array of that many slots for each new list, and one key of 40,000,000 ran
out of memory with `-Xmx64m`. With `String` keys the line does not compile, since no `ArrayList`
constructor takes a `String`. Write `k -> new ArrayList<>()` whenever the interface passes
something the constructor should not see.

A record has one canonical constructor, with its components in order, so `Reading::new` fits a
`BiFunction<String, Integer, Reading>`:

```java
BiFunction<String, Integer, Reading> reading = Reading::new;   // the canonical constructor
System.out.println(reading.apply("cellar", 12));
```

```text
Reading[sensor=cellar, celsius=12]
```

### Arrays: `String[]::new` Tells `toArray` the Element Type

`Type[]::new` is a constructor reference that takes the length, an `IntFunction<Type[]>`. Its main
use is `Stream.toArray`:

```java
Object[] plain = Stream.of("b", "a").toArray();
String[] typed = Stream.of("b", "a").toArray(String[]::new);
System.out.println(plain.getClass().getSimpleName() + " " + typed.getClass().getSimpleName());
try {
    String[] cast = (String[]) Stream.of("b", "a").toArray();
} catch (ClassCastException e) {
    System.out.println(e.getMessage().substring(0, e.getMessage().indexOf(" (")));
}
```

```text
Object[] String[]
class [Ljava.lang.Object; cannot be cast to class [Ljava.lang.String;
```

A `Stream<String>` does not know at run time that its elements are strings: the type argument is
erased. Without a generator, `toArray()` can only allocate an `Object[]`, and an array's class is
fixed when it is created, so no cast turns it into a `String[]` later. `String[]::new` lets the
stream allocate the right array.

The generator must produce an array of a reference type. `int[]::new` is a valid
`IntFunction<int[]>`, but `Stream.of(4, 9).toArray(int[]::new)` does not compile
(`inference variable A has incompatible bounds`). For a primitive array, convert with
`mapToInt(Integer::intValue)` and call `IntStream.toArray()`, which needs no generator.

## A Constructor Reference Makes a New Object per Call; a Bound Reference Reuses One

A dispatch screen turns parcel ids into slips, two ways:

```java
List<String> ids = List.of("A-7", "B-12", "C-3");
List<StringBuilder> slips = ids.stream().map(StringBuilder::new).toList();
List<StringBuilder> shared = ids.stream().map(new StringBuilder("slip ")::append).toList();
System.out.println(slips);
System.out.println(shared + " distinct objects: " + shared.stream().distinct().count());
```

Before reading on: how many `StringBuilder`s does each line create?

```text
[A-7, B-12, C-3]
[slip A-7B-12C-3, slip A-7B-12C-3, slip A-7B-12C-3] distinct objects: 1
```

`StringBuilder::new` names a constructor, so every call runs it: three ids, three builders. In
`new StringBuilder("slip ")::append`, the `new` is the receiver expression of a bound reference.
It runs once, when the reference is created (unit 4.3.1), and every call appends to that one
builder and returns it. The list holds the same object three times, so the screen shows
`slip A-7B-12C-3` three times, and code that appends a signature to one slip changes all three.

A bound reference to a mutable object is shared state. When each call needs its own object, create
it inside the call: `StringBuilder::new`, or the lambda `id -> new StringBuilder("slip ").append(id)`,
which builds `slip A-7`, `slip B-12` and `slip C-3`.

<!-- practice 2 -->

## Summary

- **`Class::method` is static or unbound, and the method's declaration decides.** Static passes
  every argument through; unbound makes the first argument the receiver. When both fit, javac
  reports an ambiguity instead of choosing. The call is virtual even when a supertype is named.
- **A bound reference holds its receiver.** Each argument goes to the method, so `suffix::concat`
  prefixes. `this::m` dispatches to the override; `super::m` always runs the parent's body.
- **`Class::new` picks its constructor from the interface's parameters.** `computeIfAbsent(key,
  ArrayList::new)` turns an `Integer` key into a capacity. `String[]::new` gives `toArray` its
  element type.
- **A constructor reference makes a new object per call; a bound reference reuses one.**
  `new StringBuilder()::append` shares one builder across every call.

## Reference

**The four kinds, with `this`/`super` and arrays as forms of bound and constructor (the
interface's method takes `a, b, …`; `x` is the value of `expr`):**

| Kind | Form | Call it makes | Example | Interface |
|---|---|---|---|---|
| Static | `Class::staticMethod` | `Class.staticMethod(a, b, …)` | `Integer::parseInt` | `Function<String, Integer>` |
| Bound | `expr::method` | `x.method(a, b, …)`, `x` evaluated once at creation | `prefix::concat` | `Function<String, String>` |
| Bound, `this`/`super` | `this::m`, `super::m` | `this.m(a, …)`, virtual; `super.m(a, …)`, the parent's body | `super::format` | `IntFunction<String>` |
| Unbound | `Class::instanceMethod` | `a.instanceMethod(b, …)`, virtual | `String::compareToIgnoreCase` | `Comparator<String>` |
| Constructor | `Class::new` | `new Class(a, b, …)`, a new object per call | `Reading::new` | `BiFunction<String, Integer, Reading>` |
| Array constructor | `Type[]::new` | `new Type[a]` | `String[]::new` | `IntFunction<String[]>` |

**Constructor references by target type (Java 21):**

| Reference | Target | Constructor |
|---|---|---|
| `ArrayList::new` | `Supplier<List<T>>` | `ArrayList()` |
| `ArrayList::new` | `Function<Integer, List<T>>` | `ArrayList(int initialCapacity)`; negative throws `IllegalArgumentException` |
| `ArrayList::new` | `Function<Collection<T>, List<T>>` | `ArrayList(Collection)`, a copy |
| `StringBuilder::new` | `Function<String, StringBuilder>` | `StringBuilder(String)` |
| `int[]::new` | `IntFunction<int[]>` | `new int[n]`; does not fit `Stream.toArray` |

## Challenges

1. Search a codebase for `computeIfAbsent(` with a `::new` argument. For each hit, what is the
   map's key type, and which constructor does it select?
2. A class registers `this::onEvent` with an event bus in its constructor, and a subclass
   overrides `onEvent` using a field it sets in its own constructor. What happens to an event
   delivered during construction, and would `super::onEvent` be any better?
3. A team rule says "prefer method references to lambdas". Write the exceptions this page gives
   you, and decide whether a reviewer could spot each one from the line alone.

## Design Note: Why `::` Has No Parameter List

A method reference names a method and lets the target type say how to call it. Leaving out a
parameter list keeps `String::length` and `ArrayList::new` short, and lets one spelling serve
several shapes: `ArrayList::new` is a `Supplier`, a `Function<Integer, …>` and a
`Function<Collection, …>`. The cost is that the line does not say which shape it has. javac turns the static-or-unbound question into an error when both fit, but it cannot tell
an intended constructor from an accidental one, and `computeIfAbsent(id, ArrayList::new)` compiles.
Where the choice matters, a lambda states the constructor and its arguments.

## Code Examples

- Source: [MethodReferenceTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceTypes.java)
- Test: [MethodReferenceTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceTypesTest.java)
