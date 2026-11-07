# 1.5.1 instanceof Operator in Java

A service loads each plugin through a class loader of its own. The first request to a plugin
fails with `ClassCastException: class Greeter cannot be cast to class Greeter`, and the
`instanceof Greeter` test a line earlier was `false`, though both classes have the same name and
the same bytecode.

Pattern variables are unit 1.5.2; record patterns and `switch`, units 1.5.3 and 1.5.4.

## instanceof Asks About the Object's Class at Run Time, and null Has None

A YAML reader filled this configuration map:

```java
Map<String, Object> config = new HashMap<>();
config.put("timeout", 30L);                      // the YAML reader chose Long
Object retries = config.get("retries");          // not configured: null
Object timeout = config.get("timeout");
System.out.println("retries instanceof Integer: " + (retries instanceof Integer));
System.out.println("(Integer) retries: " + (Integer) retries);
System.out.println("timeout instanceof Number: " + (timeout instanceof Number)
        + ", Comparable: " + (timeout instanceof Comparable)
        + ", Integer: " + (timeout instanceof Integer));
System.out.println("Number.class.isInstance(timeout): " + Number.class.isInstance(timeout));
int attempts = (Integer) retries;
```

```text
retries instanceof Integer: false
(Integer) retries: null
timeout instanceof Number: true, Comparable: true, Integer: false
Number.class.isInstance(timeout): true
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because "retries" is null
```

The test ignores the declared type, `Object` here, and asks whether the class of the object the
variable refers to is `T` or a subtype of `T`. A `Long` is a `Number` and a `Comparable`, never an `Integer`. `null` refers to no object, so
the test is `false` for every `T`. `Class.isInstance` is the same test with the type as a value.

The cast disagrees on `null`. It checks the class and throws `ClassCastException` on a mismatch
(unit 1.1.3), but it lets `null` through, so the missing key surfaces later, as a
`NullPointerException` where the `Integer` is unboxed. Test a value that may be absent before
casting it.

### A Class Is Its Name and the Loader That Defined It

The plugin from the start of the unit, reduced: a `URLClassLoader` with no parent defines its own
`Greeter` from the same class file the application loaded.

```java
Object greeter = loader.loadClass("Greeter").getDeclaredConstructor().newInstance();
System.out.println("class name: " + greeter.getClass().getName());
System.out.println("greeter instanceof Greeter: " + (greeter instanceof Greeter));
Greeter g = (Greeter) greeter;
```

```text
class name: Greeter
greeter instanceof Greeter: false
Exception in thread "main" java.lang.ClassCastException: class Greeter cannot be cast to class Greeter (Greeter is in unnamed module of loader java.net.URLClassLoader @5c647e05; Greeter is in unnamed module of loader 'app')
```

At run time the JVM identifies a class by its name and the loader that defined it. The caller's
`Greeter` is the application loader's, so the plugin's object is of an unrelated class. A cache that keeps objects across a reload hands out instances of the old classes. Load the
shared type from a parent loader both sides use, or drop the old objects with the old loader.

<!-- practice 1 -->

## The Compiler Rejects a Test That Can Never Be True, Judging Only the Declared Types

```java
static void check(Integer count, Thread worker, String name, int[] ids) {
    boolean a = count instanceof String;
    boolean b = worker instanceof List;
    boolean c = name instanceof Runnable;
    boolean d = ids instanceof Object[];
    String s = (String) count;
}
```

```text
Impossible.java:5: error: incompatible types: Integer cannot be converted to String
Impossible.java:7: error: incompatible types: String cannot be converted to Runnable
Impossible.java:8: error: incompatible types: int[] cannot be converted to Object[]
Impossible.java:9: error: incompatible types: Integer cannot be converted to String
```

javac accepts `x instanceof T` only if a cast from `x`'s declared type to `T` could succeed for some
object. No object is both an `Integer` and a `String`. A `Thread` can be a `List`, since a subclass
of `Thread` may implement it, so the `worker` test compiles and is decided at run time. `String` is
`final`, as are `Integer` and `Long`, and it doesn't implement `Runnable`, so no subclass can add it.
The cast follows the same rule.

Against an `Object` parameter every test compiles, even one no caller can satisfy.

> **Primitive types.** Java 21 rejects `x instanceof Integer` for an `int x`: the left side must be
> a reference. Java 25's javac reports it as a preview feature ("primitive patterns").

<!-- practice 4 -->

## Arrays Keep Their Element Type at Run Time; Generic Types Lose Their Type Arguments

```java
Object names = new String[] {"ada", "grace"};
Object untyped = new Object[] {"ada", "grace"};
Object counts = new int[] {1, 2};
System.out.println("names: Object[] " + (names instanceof Object[]) + ", String[] " + (names instanceof String[]));
System.out.println("untyped: String[] " + (untyped instanceof String[]));
System.out.println("counts: Object[] " + (counts instanceof Object[]) + ", Object " + (counts instanceof Object));
Object[] slots = (Object[]) names;
slots[0] = 42;                                       // caught and printed

Object payload = new ArrayList<>(List.of(7, 8));     // integers, from another module
System.out.println("payload instanceof List<?>: " + (payload instanceof List<?>));
List<String> tags = (List<String>) payload;         // unchecked: checks only List
System.out.println("cast done, size " + tags.size());
String first = tags.get(0);
```

```text
names: Object[] true, String[] true
untyped: String[] false
counts: Object[] false, Object true
java.lang.ArrayStoreException: java.lang.Integer
payload instanceof List<?>: true
cast done, size 2
Exception in thread "main" java.lang.ClassCastException: class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
```

An array's class is fixed when it is created, whatever it holds later: `untyped` holds only strings
and is still no `String[]`. A `String[]` is also an `Object[]`, and the array checks every store,
hence the `ArrayStoreException`. An `int[]` holds no references, so it is an `Object` but not an
`Object[]`.

A generic type keeps no type argument at run time: `ArrayList<String>` and `ArrayList<Integer>` are
one class. The cast to `List<String>` could check only `List`, and javac flags it only as unchecked.
The `ClassCastException` comes from a cast the compiler inserted at `tags.get(0)`, in whatever code
reads the list, as with the varargs array in unit 1.3.2. For the same reason javac refuses
`payload instanceof List<String>` for an `Object payload`:

```text
GenericTests.java:7: error: Object cannot be safely cast to List<String>
```

Since Java 16 a type argument is allowed where the declared type already guarantees it:
`tags instanceof ArrayList<String>` compiles for a `List<String> tags`. Before that, only a raw type
or `<?>` was. To get a `List<String>` out of an `Object`, test `List<?>`, then test every element.

<!-- practice 2 -->

## In equals, instanceof Breaks Symmetry With a Subclass, and getClass() Refuses Every Subclass

Unit 1.3.4 fixed an `equals(Sku)` overload with an `equals(Object)` that starts with a type test.
Here both classes test with `instanceof`, and the subclass adds a field:

```java
import java.util.Objects;

class Point {
    final int x, y;
    Point(int x, int y) { this.x = x; this.y = y; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Point)) return false;
        Point p = (Point) o;
        return x == p.x && y == p.y;
    }
    @Override public int hashCode() { return Objects.hash(x, y); }
}

class LabeledPoint extends Point {
    final String label;
    LabeledPoint(int x, int y, String label) { super(x, y); this.label = label; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof LabeledPoint)) return false;
        LabeledPoint p = (LabeledPoint) o;
        return super.equals(p) && label.equals(p.label);
    }
}
```

Output for `depot = new Point(3, 4)` and `gate = new LabeledPoint(3, 4, "gate B")`, then with
`if (o == null || getClass() != o.getClass()) return false;` in place of each `instanceof` line,
then against an anonymous `new Point(3, 4) { }`:

```text
instanceof:  depot.equals(gate) true, gate.equals(depot) false
             List.of(depot).contains(gate) false, List.of(gate).contains(depot) true
             HashSet size, depot added first 2, gate added first 1
getClass():  depot.equals(gate) false, gate.equals(depot) false
             depot.equals(subclass adding nothing) false
```

`Object.equals` requires symmetry: `x.equals(y)` is true exactly when `y.equals(x)` is. `depot`
accepts any `Point`; `gate` only a `LabeledPoint`. `contains` calls `equals` on its argument, so `contains` answers differently depending on which of the
two the list holds, and a `HashSet` keeps one or both depending on which was added first.

`getClass()` restores symmetry by refusing every other class, even a subclass that adds no field.
A `final` value class avoids the choice, since `instanceof` is then exact; every record is final.
When a type needs more state, hold a `Point` in a field instead of extending it.

<!-- practice 3 -->

## Summary

- **`instanceof` tests the run-time class of the object, subtypes included, and is `false` for
  `null`.** A cast lets `null` through, and a class is its name plus its loader, so a reloaded
  class is a different type.
- **The compiler rejects a test that can never be true, from the declared types only.** An
  interface test compiles unless the class is `final` and doesn't implement it; an `Object` parameter turns the check off.
- **Arrays know their element type; generics don't.** An `int[]` is not an `Object[]`, and an
  unchecked cast to `List<String>` fails later, where an element is read.
- **In `equals`, `instanceof` with a subclass that adds state breaks symmetry; `getClass()`
  refuses all subclasses.** Make value classes `final`, or compose instead of extending.

## Reference

| Expression | Result |
|---|---|
| `null instanceof T`, `T.class.isInstance(null)` | `false` |
| `(T) null`, `T.class.cast(null)` | `null`, no exception |
| `new int[1] instanceof Cloneable`, `Serializable` | `true` |
| `Number.class.isAssignableFrom(Long.class)` | `true` (the reverse is `false`) |
| `new ArrayList<String>().getClass() == new ArrayList<Integer>().getClass()` | `true` |

## Challenges

1. Your service decodes JSON into `Map<String, Object>`. Find every cast that follows a `get` and
   decide which should test, which should fall back to a default, and which should fail loudly.
2. A library's class hierarchy uses `instanceof` in `equals`. You must subclass it and add a field.
   What are your options, and what does each cost?
3. How does your framework's development-mode restart load your classes, and which cached
   objects could outlive a restart?

## Design Note: Why Java Lets Code Ask an Object's Class

Polymorphism is the usual way to vary behavior by type: each class overrides `area()`, and the
caller calls it. A chain of `instanceof` tests does the same from outside the classes and must be
edited for every new subtype; one it misses falls to the `else` without a warning. Java still needs the test where code receives an object whose type nobody could
declare: `equals(Object)`, deserialized data, a plugin. Sealed types with a `switch` (unit 1.5.4)
bring the compiler back into such a chain, which then fails to compile when a subtype is added and
not handled.

## Code Examples

- Test: [InstanceofTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/patternmatching/InstanceofTest.java)
- Source: [InstanceofImpl.java](src/main/java/com/github/msorkhpar/claudejavatutor/patternmatching/InstanceofImpl.java)
