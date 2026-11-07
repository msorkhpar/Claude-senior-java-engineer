# 1.3.2 Method Parameters and Return Types in Java

A shared utility library ships a release, and three teams file bugs in the same week. The audit
helper `audit(String event, Object... fields)` throws `NullPointerException` on a call that passed
`null` as its one field. A nightly report removed a nurse from the on-call shift it was only
supposed to print. And a generic `pair(a, b)` throws `ClassCastException` on a line with no cast in
it.

Each one is a parameter or return type that allowed more than its author meant: an array the
caller still owned, a list the caller could edit, a generic type that was gone at run time.
Declaring and calling a method is unit 1.3.1, and what passing a reference copies is unit 1.3.3.
Every output is from Java 21.

## A Varargs Parameter Is an Array: the Caller Can Pass Its Own, or null

`Object... fields` is an `Object[]` parameter. The compiler builds the array only when the call
lists loose arguments; an array that is already an `Object[]`, or a `null`, is passed as is. Before reading the output: how many
fields does each call pass?

```java
static String audit(String event, Object... fields) {
    return event + " " + fields.length + " field(s)";
}

audit("login");
audit("login", (Object) null);
audit("login", names);              // String[] names = {"guest", "admin"}
audit("login", (Object) names);
audit("login", null);
```

```text
login 0 field(s)
login 1 field(s)
login 2 field(s)
login 1 field(s)
audit("login", null): NullPointerException
```

A `String[]` or a `null` already fits `Object[]`, so it is passed as the array itself: `fields` is
`null`, and `fields.length` throws. A cast to `Object` makes either one a single element. javac
warns about both calls:

```text
Varargs.java:21: warning: non-varargs call of varargs method with inexact argument type for last parameter;
            System.out.println(audit("login", null));
                                              ^
  cast to Object for a varargs call
  cast to Object[] for a non-varargs call and to suppress this warning
```

The method holds the caller's array, not a copy, so a helper that sorts its varargs to find the
largest value reorders the caller's data:

```java
static int largest(int... values) {
    Arrays.sort(values);                 // sorts the array it was given
    return values[values.length - 1];
}
```

```text
largest: 9, readings now: [1, 3, 7, 9]
```

A method that stores its varargs in a field keeps the caller's array too, which the caller can
change later. Copy it on the way in, and decide what a `null` array means.

A primitive array is never an `Object[]`, so an `int[]` passed to `Object...` becomes one element.
`Arrays.asList(T... a)` can't spread an `int[]` either, because `T` can't be `int`:

```text
Arrays.asList(int[]).size(): 1
Arrays.asList(Integer[]).size(): 3
```

For an `int[] ids` that holds 8, `Arrays.asList(ids).contains(8)` is `false`.

<!-- practice 1 -->

## A Generic Varargs Array Has the Erased Type, So Handing It Out Fails Somewhere Else

### Spot the Problem: a Generic Method That Returns Its Varargs Array

```java
static <T> T[] toArray(T... items) {
    return items;                                  // hands out the array the compiler built
}

static <T> T[] pair(T first, T second) {
    return toArray(first, second);
}

String[] direct = toArray("a", "b");               // works: the array is a String[]
String[] viaPair = pair("a", "b");                 // which line throws, and why?
```

```text
toArray("a", "b"): String[]
pair("a", "b"): class [Ljava.lang.Object; cannot be cast to class [Ljava.lang.String; ([Ljava.lang.Object; and [Ljava.lang.String; are in module java.base of loader 'bootstrap')
```

The compiler creates the varargs array at the call site, with the type it knows there. In `main`,
`T` is `String`, so `toArray` gets a `String[]`. Inside `pair`, `T` is a type variable, erased to
`Object` at run time, so the array built there is an `Object[]`. `pair` returns it as a `T[]`, and
the cast the compiler inserted into `String[] viaPair = ...` fails, on a line with no cast, two
calls away from the cause. Copying the array doesn't help: `Arrays.copyOf` returns an array of
exactly the class of the one it copies, so the copy is an `Object[]` too, and the cast at the
caller fails the same way.

A plain build prints only a note about "unchecked or unsafe operations". With `-Xlint:unchecked`,
javac names both methods:

```text
Generic.java:6: warning: [unchecked] Possible heap pollution from parameterized vararg type T
    static <T> T[] toArray(T... items) {
                                ^
  where T is a type-variable:
    T extends Object declared in method <T>toArray(T...)
Generic.java:11: warning: [unchecked] unchecked generic array creation for varargs parameter of type T[]
        return toArray(first, second);
                      ^
  where T is a type-variable:
    T extends Object declared in method <T>pair(T,T)
```

Heap pollution means a variable of a parameterized type refers to an object that isn't of that
type. A `List<String>...` parameter shows the other way it happens:

```java
static void addAll(List<String>... lists) {
    Object[] array = lists;
    array[0] = List.of(42);                        // compiles, and the store check can't see <String>
    String s = lists[0].get(0);                    // the ClassCastException lands here
}
```

```text
addAll: class java.lang.Integer cannot be cast to class java.lang.String (java.lang.Integer and java.lang.String are in module java.base of loader 'bootstrap')
```

`@SafeVarargs` on `toArray` silences the warnings at its call sites, and `pair("a", "b")` still
throws: the annotation is the author's promise, and the compiler doesn't check it (`-Xlint:varargs`
even flags the `return items`). It is allowed only on constructors and on methods that can't be
overridden (static, final or private); on an instance method that is neither final nor private it
is a compile error. The
promise holds when the method only reads the elements and never hands the array out as a `T[]` or
writes into it. `List.of` and `Arrays.asList` carry the annotation and return a `List<T>`, never the
array.

## A Returned Collection Is Whatever the Caller Can Do With It

A `Shift` keeps its staff in an `ArrayList`, and `staff()` returns that field. The night report
should list who is on call, without trainees (names starting with `b`):

```java
List<String> onCall = night.staff();
onCall.removeIf(n -> n.startsWith("b"));        // the report only wanted to list who is on call
```

```text
shift after the report: [ana, cy], size 2
```

A `List<String>` has `removeIf`, so returning the field gave the report write access to the
shift. A method that returns internal state returns a copy (the
caller's own, editable), a `List.copyOf` snapshot (immutable) or an unmodifiable view (read-only,
but it shows later changes); unit 1.1.2 has the table of what each one shares.

The opposite surprise is a result that is only sometimes unmodifiable. This module's example
class, `MethodParametersAndReturnTypes`, has a `generateEvenNumbers` that returns `Collections.emptyList()` for a negative count and an `ArrayList`
otherwise:

```text
generateEvenNumbers(2).add(100): ok [0, 2, 100]
generateEvenNumbers(0).add(100): ok [100]
generateEvenNumbers(-1).add(100): UnsupportedOperationException
```

A caller that adds to the result passes every test written with ordinary counts and fails in
production on the first negative one. The type can't say which kind of list comes back, so the
documentation must, and every path of the method must return the same kind.

<!-- practice 2 -->

## Put Absence in the Return Type: an Empty Optional or an Empty Collection, Never null

A directory lookup returns `null` for a user it doesn't know. The caller checks the domain:

```java
static String emailOrNull(String user) { return EMAILS.get(user); }

boolean internal = emailOrNull("cy").endsWith("@example.com");
```

```text
Cannot invoke "String.endsWith(String)" because the return value of "Absent.emailOrNull(String)" is null
```

Nothing in `String emailOrNull(String)` says the result can be missing, so the caller didn't
check. `Optional<String> email(String)` says it in the type, and the caller has to unwrap the
result before it can call `endsWith`. `askDirectory` below is a remote call:

```java
static Optional<String> email(String user) { return Optional.ofNullable(EMAILS.get(user)); }
static Optional<String> emailOf(String user) { return Optional.of(EMAILS.get(user)); }

String a = email("ana").orElse(askDirectory("ana"));
String b = email("ana").orElseGet(() -> askDirectory("ana"));
```

```text
email("cy"): Optional.empty
Optional.of(null): NullPointerException
orElse, value present:     ana@example.com, directory calls: 1
orElseGet, value present:  ana@example.com, directory calls: 0
```

`Optional.of` is for a value that can't be null, and throws if it is; a value that may be null is
wrapped with `ofNullable`. `orElse(x)` is an ordinary method call, so `x` is evaluated before
`orElse` runs, whether or not a value is present: here, a remote call on every lookup. `orElseGet`
takes a `Supplier` and calls it only when the `Optional` is empty.

For a collection, absence is the empty collection: a caller loops over it with no check, and the
loop runs zero times. `Optional<List<T>>` gives the caller two kinds of nothing to handle. The
javadoc intends `Optional` for return types; as a parameter or a field it adds a third state,
since the `Optional` itself can be `null`.

<!-- practice 3 -->

## An Override May Narrow the Return Type and the Checked Exceptions, Never Widen Them

A caller holding a `Shape` relies on `copy()` returning a `Shape`. An override that returns a
`Circle` keeps that promise and makes a stronger one, so Java allows it (a covariant return type):

```java
class Shape {
    Shape copy() { return new Shape(); }
}

class Circle extends Shape {
    @Override
    Circle copy() { return new Circle(); }        // a narrower return type
}
```

`new Circle().copy()` is a `Circle` with no cast. The JVM identifies a method by its name,
parameter types and return type, so javac adds a bridge method to `Circle` for callers that hold a
`Shape`:

```text
copy returns Circle, bridge=false
copy returns Shape, bridge=true
```

A wider return type breaks the promise: had `Circle.copy()` returned `Object`, javac would refuse
it with `return type Object is not compatible with Shape`. The check is against the method an
override replaces directly, so once `Circle.copy()` returns `Circle`, a subclass of `Circle` can't
go back to `Shape`.

The `throws` clause works the same way. An override may declare fewer or narrower checked
exceptions than the method it overrides, never broader ones:

```text
Widen.java:10: error: read() in FileSource cannot implement read() in Source
        public String read() throws Exception {        // broader than IOException
                      ^
  overridden method does not throw Exception
```

What a caller must catch comes from the type it calls through, not from the object. `CachedPrice`
implements `Callable<Integer>` with a `call()` that declares nothing. Called on a `CachedPrice`
variable, it needs no `try`. Called through a `Callable<Integer>` variable, the same object needs
one, because `Callable.call()` declares `throws Exception`:

```text
Caller.java:11: error: unreported exception Exception; must be caught or declared to be thrown
        return task.call();
                        ^
```

So every caller that holds the interface type must catch or declare `Exception`, whatever the
implementation does.

`final` on a parameter is not part of the
signature at all: it stops the method's own body from reassigning the parameter (`final parameter
n may not be assigned`), an override may drop it, and the caller can't tell.

<!-- practice 4 -->

## Summary

- **A varargs parameter is an array.** An argument that is already an array of the parameter's
  type is passed as is, so the method shares the caller's array, and a `null` argument arrives as a `null` array; `Arrays.asList(int[])` has one
  element.
- **A generic varargs array has the erased type.** Returning or storing it causes a
  `ClassCastException` in a caller with no cast; `@SafeVarargs` is an unchecked promise.
- **A returned collection is whatever the caller can do with it.** Return a copy, a snapshot or a
  view, never the field, and the same kind on every path.
- **Absence belongs in the return type.** `Optional` for one value, an empty collection for many;
  `Optional.of(null)` throws, and `orElse` evaluates its argument every time.
- **An override may narrow the return type and the checked exceptions, never widen them.** Callers
  catch what the type they call through declares; `final` on a parameter is invisible to them.

## Reference

**What a varargs method receives (`m(Object... a)`, Java 21):**

| Call | `a` |
|---|---|
| `m()` | an empty array |
| `m(x, y)` | a new `Object[]` of 2 |
| `m(stringArray)` | the caller's array itself (javac warns) |
| `m((Object) stringArray)` | a new `Object[]` of 1 holding the array |
| `m(intArray)` | a new `Object[]` of 1 holding the `int[]` |
| `m(null)` | `null` (javac warns) |
| `m((Object) null)` | a new `Object[]` of 1 holding `null` |

A varargs parameter must be the last parameter, and a method has at most one.

**What an override may change (JLS 8.4.8):**

| Part of the signature | Override may |
|---|---|
| parameter types | not change (a different list is an overload, unit 1.3.4) |
| return type (reference) | keep, or narrow to a subtype |
| return type (primitive, `void`) | not change |
| checked exceptions | drop, or narrow to subclasses |
| unchecked exceptions | declare any |
| `final` on a parameter | add or drop |

## Challenges

1. A logging API takes `(String format, Object... args)`. Callers pass `null`, arrays and nothing
   at all. Decide what each should log, and whether an overload `(String format, Object arg)` makes
   it better or worse (unit 1.3.4).
2. A service returns a 50,000-element list from a cache on every request. Compare a copy,
   `List.copyOf` and an unmodifiable view by what each allocates and what callers can still break.
3. Your interface method declares `throws IOException`, and only one of five implementations does
   any I/O. Keep the clause, remove it, or split the interface? Name who pays in each case.

## Design Note: Why Varargs Is an Array

Varargs arrived in Java 5 as a compile-time rewrite: the method is compiled with an array
parameter marked as varargs, and each call site builds the array. The JVM needed no change, and an
existing method that took an `Object[]`, such as reflection's `Method.invoke`, could become a
varargs method without breaking its callers.

The price is that the array is visible. A caller can pass its own or `null`, and the method can
keep it or return it. Generics arrived in the same release, with erasure, and an array of a type
variable can't be created with its real type, so `T...` builds an array of the erasure and the
language needed `@SafeVarargs` to mark the safe uses. A `List` parameter would have avoided the
generic array problem, at the cost of an object per call and of compatibility with every method that already took an
array.

## Code Examples

- Test: [MethodParametersAndReturnTypesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/MethodParametersAndReturnTypesTest.java)
- Source: [MethodParametersAndReturnTypes.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/MethodParametersAndReturnTypes.java)
