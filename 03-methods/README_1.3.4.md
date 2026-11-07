# 1.3.4 Method Overloading in Java

One release of the inventory service brought three bugs. A SKU that is in the stock set is
reported missing. The cleanup that retires the 1-second retry delay removes the 5-second one. An
exported JSON array has no opening bracket. Overloads are methods with one name and different
parameter lists, and the compiler picks one per call. Every line responsible compiled without a
warning, and each call went to an overload its author didn't mean. Every output below is from
Java 21.

## The Compiler Chooses an Overload From the Arguments' Declared Types

### Spot the Problem: an equals That Works Until a Collection Calls It

```java
import java.util.Objects;

final class Sku {
    final String code;
    Sku(String code) { this.code = code; }

    public boolean equals(Sku other) { return other != null && code.equals(other.code); }
    @Override public int hashCode() { return Objects.hash(code); }
}
```

```java
Sku a = new Sku("TEA-250");
Sku b = new Sku("TEA-250");
Set<Sku> stocked = new HashSet<>(List.of(a));
Object asObject = b;
System.out.println("a.equals(b):        " + a.equals(b));
System.out.println("a.equals(asObject): " + a.equals(asObject));
System.out.println("stocked.contains(b): " + stocked.contains(b));
System.out.println("List.of(a).contains(b): " + List.of(a).contains(b));
```

```text
a.equals(b):        true
a.equals(asObject): false
stocked.contains(b): false
List.of(a).contains(b): false
```

`equals(Sku)` doesn't override `Object.equals(Object)`: its parameter list differs, so it is an
overload. At each call the compiler searches the methods of the receiver's declared type, compares
the declared types of the arguments, and records which overload to call. With `b` declared `Sku` it chose `equals(Sku)`. `a.equals(asObject)`
passes an `Object`, and so does the JDK's code in `HashSet` and `List`, so those calls go to
`Object.equals`, which compares references. The object's class at run time takes part in only one choice: which
override of the chosen method runs on the receiver (unit 1.3.1).

The unit tests called `a.equals(b)` and passed, while every collection missed the SKU. Add
`@Override` and javac rejects the method: `method does not override or implement a method from a
supertype`. The fix takes an `Object`:
`@Override public boolean equals(Object o) { return o instanceof Sku other && code.equals(other.code); }`.
When behaviour must depend on an argument's class at run time, put the method on that class, or
test the class with a type pattern, as this `equals` does.

<!-- practice 1 -->

## Widening First, Then Boxing, Then Varargs: the First Phase That Fits Wins

The compiler looks for an applicable overload in three phases. The first allows only subtyping and
widening (`int` to `long`, `long` to `double`, `char` to `int`), the second adds boxing and unboxing, and the third
varargs. It stops at the first phase that finds a candidate and takes the most specific one there.
Predict each line:

```java
static String track(long value)    { return "track(long)"; }
static String track(Object value)  { return "track(Object)"; }

static String tag(Object value)    { return "tag(Object)"; }
static String tag(int... values)   { return "tag(int...)"; }
```

```java
int count = 3;
Integer boxed = 3;
System.out.println("track(count) -> " + track(count));
System.out.println("track('x')   -> " + track('x'));
System.out.println("track(boxed) -> " + track(boxed));
System.out.println("tag(count)   -> " + tag(count));
System.out.println("tag(1, 2)    -> " + tag(1, 2));
```

```text
track(count) -> track(long)
track('x')   -> track(long)
track(boxed) -> track(Object)
tag(count)   -> tag(Object)
tag(1, 2)    -> tag(int...)
```

`char` is an integral type, so `'x'` widens to `long` in the first phase. An `Integer` is
already an `Object`, so the first phase finds `track(Object)`, and unboxing to `long` is never
tried. A single `int` reaches `tag(Object)` by boxing, before varargs is considered. The first
phase matches how Java chose overloads before version 5 added boxing and varargs, so calls written
before then still choose the same method.

The JSON exporter's missing bracket is the `char` rule in the JDK:

```java
StringBuilder json = new StringBuilder('[');
json.append("1,2").append(']');
System.out.println(json);
```

```text
1,2]
```

`StringBuilder` has no `char` constructor. `'['` widens to the `int` 91, so javac picks
`StringBuilder(int capacity)`.

<!-- practice 2 -->

## On a List of Integers, remove(int) Takes an Index, Not a Value

`List` has `remove(int index)` and `remove(Object o)`. An `int` argument fits `remove(int)` in the
first phase, so it is never boxed:

```java
List<Integer> retryDelays = new ArrayList<>(List.of(30, 5, 1, 60));
int retired = 1;
retryDelays.remove(retired);
System.out.println("remove(retired):                  " + retryDelays);

retryDelays = new ArrayList<>(List.of(30, 5, 1, 60));
retryDelays.remove(Integer.valueOf(retired));
System.out.println("remove(Integer.valueOf(retired)): " + retryDelays);

java.util.Collection<Integer> asCollection = new ArrayList<>(List.of(30, 5, 1, 60));
asCollection.remove(retired);
System.out.println("as a Collection, remove(retired): " + asCollection);

try {
    new ArrayList<>(List.of(30, 5, 1, 60)).remove(60);
} catch (IndexOutOfBoundsException e) {
    System.out.println("remove(60): " + e.getMessage());
}

Set<Long> blocked = new HashSet<>(List.of(7L, 8L));
int userId = 7;
System.out.println("blocked.remove(userId): " + blocked.remove(userId) + ", " + blocked);
```

```text
remove(retired):                  [30, 1, 60]
remove(Integer.valueOf(retired)): [30, 5, 60]
as a Collection, remove(retired): [30, 5, 60]
remove(60): Index 60 out of bounds for length 4
blocked.remove(userId): false, [7, 8]
```

The 5-second delay went, and the 1 stayed. A value that isn't a valid index throws, so the bug
is loud only for large values; whenever the value happens to be a valid index, the wrong element
goes without an error. The
same call through a variable declared `Collection<Integer>` removes the value, because
`Collection` has only `remove(Object)`. `Integer.valueOf(retired)` states the intent. The last line
is the trap from unit 1.1.3: on a `Set<Long>`, `remove` boxes an `int` to an `Integer`, which never
equals a `Long`, and removes nothing.

<!-- practice 3 -->

## null Goes to the Most Specific Overload, and Two Unrelated Ones Don't Compile

With `audit(Object)` and `audit(String)`, `audit(null)` calls `audit(String)`: both fit, and
`String` is more specific because every `String` is an `Object`. Add `audit(Integer)`, and the
same call stops compiling:

```text
Ambiguous.java:7: error: reference to audit is ambiguous
        System.out.println(audit(null));
                           ^
  both method audit(String) in Ambiguous and method audit(Integer) in Ambiguous match
1 error
```

Neither `String` nor `Integer` is more specific than the other. A cast picks one:
`audit((String) null)`. So a library release that only adds an overload can break its callers'
builds. `PrintStream` shows both sides. `System.out.println(null)` doesn't compile, since
`println(char[])` and `println(String)` both match. A `char[]` variable holding `null` compiles,
and throws:

```java
String noName = null;
System.out.println(noName);
char[] noChars = null;
System.out.println(noChars);
```

```text
null
Exception in thread "main" java.lang.NullPointerException: Cannot read the array length because "cbuf" is null
```

## Overloads Must Differ in Erased Parameter Types: the Return Type and Type Arguments Don't Count

A class can't declare two methods with the same signature, and a signature is the name and the
parameter types: the return type isn't part of it, and type arguments are erased from it. Both of
these fail:

```text
ReturnType.java:3: error: method parse(String) is already defined in class ReturnType
    static long parse(String text)    { return Long.parseLong(text); }
                ^
1 error
Erasure.java:5: error: name clash: total(List<Double>) and total(List<Integer>) have the same erasure
    static double total(List<Double> prices)    { return prices.size(); }
                  ^
1 error
```

Give them different names, such as `totalQuantity` and `totalPrice`.

<!-- practice 4 -->

## Summary

- **The compiler chooses an overload from the arguments' declared types.** A narrower `equals`
  is an overload that collections never call; `@Override` catches it.
- **Widening first, then boxing, then varargs.** A `char` goes to an `int` or `long` overload,
  and an `Integer` to `Object` before it would unbox.
- **On a `List<Integer>`, an `int` argument is an index.** Box it to remove the value.
- **`null` goes to the most specific overload**, and adding an unrelated one makes the call
  ambiguous.
- **Overloads differ in erased parameter types.** The return type and type arguments don't count.

## Reference

**Overloading and overriding:**

| | Overloading | Overriding |
|---|---|---|
| parameter types | differ after erasure | the same |
| chosen | at compile time, from the arguments' declared types | at run time, by the receiver's class |
| return type | any | the same, or a subtype for a reference type |
| `static` and `private` methods | can be overloaded | aren't overridden; a `static` one is hidden (unit 1.3.1) |

**Choosing an overload (JLS 15.12.2):**

| Phase | Conversions allowed |
|---|---|
| 1 | identity, widening primitive, subtyping |
| 2 | phase 1, plus boxing and unboxing |
| 3 | phase 2, plus varargs |

Within the first phase that finds candidates, the most specific wins; if no candidate is more
specific than all the others, the call is ambiguous and doesn't compile.

## Challenges

1. Search your code for `remove(` on lists of `Integer` or `Long`. Which calls pass a value that
   is meant as a value?
2. Your logging API has `log(String, Object...)`. A colleague proposes adding
   `log(String, Object)` for speed. Which existing calls change overload, and could any break?
3. Would you overload `send(Email)` and `send(Sms)`, or give each its own name? Consider a caller
   that holds a `Message`.

## Design Note: Why Overloads Are Bound at Compile Time

Choosing at compile time is cheap and reports ambiguity before anything runs. The choice is
written into the caller's class file, so a new overload reaches callers only when they are
recompiled. A library adds `record(long)` beside `record(Object)`, and a caller of
`Metrics.record(5)` runs against it before and after recompiling:

```text
compiled against v1, run on v1:
record(Object): "5"
compiled against v1, run on v2:
record(Object): "5"
recompiled against v2, run on v2:
record(long): 5
```

Two services built from the same source, one rebuilt and one not, call different methods. Treat
an overload that existing calls could fit better as a behaviour change, not an addition.

## Code Examples

- Test: [MethodOverloadingTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/MethodOverloadingTest.java)
- Source: [MethodOverloading.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/MethodOverloading.java)
