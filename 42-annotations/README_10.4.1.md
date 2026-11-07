# 10.4.1. Built-in Annotations (@Override, @Deprecated, @SuppressWarnings)

The payments team upgraded its shared retry library from version 1 to version 2. The build was
green, javac printed nothing, and in production every retry now waits a full second: the team's
backoff of 100, 200, 400 ms no longer runs. Version 2 had added a parameter to the method the team
overrode.

The annotations in `java.lang` are instructions to javac. `@Override` and `@Deprecated` add a
check; `@SuppressWarnings` and `@SafeVarargs` switch one off. Every output is from Java 21. `@FunctionalInterface` is
in unit 4.1.3, and how long an annotation survives is unit 10.4.3.

## @Override Turns a Method That Stopped Overriding into a Compile Error

Version 1 of `Retry` declares `protected long delayMillis(int attempt)`, version 2
`delayMillis(int attempt, Exception cause)`. Both return 1000, and a final `plan(n)` lists the
delay for each of `n` attempts. The team's subclass:

```java
/** Our code: doubles the delay on each attempt. */
public class Backoff extends Retry {
    protected long delayMillis(int attempt) { return 100L << (attempt - 1); }

    public static void main(String[] args) {
        System.out.println("delays: " + new Backoff().plan(4));
    }
}
```

Compiled and run against each version:

```text
v1 compiled
delays: 100 200 400 800
v2 compiled
delays: 1000 1000 1000 1000
```

Against version 2, `delayMillis(int)` matches no method of `Retry`, so it is a new method of
`Backoff`, an overload that nothing calls, and javac has nothing to report. With `@Override` on
it, the same upgrade fails the build on the line that has to
change:

```text
Backoff.java:3: error: method does not override or implement a method from a supertype
    @Override protected long delayMillis(int attempt) { return 100L << (attempt - 1); }
    ^
1 error
```

A method overrides by its signature, with or without the annotation. `@Override` adds a claim,
"this overrides something", and javac checks it against the supertypes each time the subclass
compiles, so it also catches a change someone else makes to the supertype later. The check runs
only at compile time: the annotated `Backoff`, compiled against version 1 and run with version 2's `Retry.class`
on the class path, printed `delays: 1000 1000 1000 1000` too. A plugin built against one version
of a library and deployed into a host that ships another gets no check at all.

A misspelt name does the same to the methods every class overrides. Here `equals` is right and
`hashCode` is written `hashcode`:

```java
final class AccountId {
    private final String value;
    AccountId(String value) { this.value = value.strip().toUpperCase(); }

    @Override public boolean equals(Object o) {
        return o instanceof AccountId other && value.equals(other.value);
    }
    public int hashcode() { return value.hashCode(); }
}
```

`new AccountId("acc-1042")` and `new AccountId(" ACC-1042 ")` both go into a `HashSet`:

```text
equals: true, accounts in the set: 2
```

`HashSet` calls `hashCode()`, which is still `Object`'s identity hash, so the two equal IDs almost
always have different hash codes, and `HashSet` never calls `equals`. `@Override public int hashcode()`
fails with the same error. Unit 1.3.4 shows the overload form, `equals(Sku)`, and unit 1.3.1
`@Override` on a static method.

## A Use of a forRemoval Element Warns by Default; Plain Deprecation Is Only a Note

A payment library deprecates two methods, one of them for removal:

```java
/** A payment library, version 4. */
public class Gateway {
    /** @deprecated use {@link #capture(String, long)}, which is idempotent */
    @Deprecated(since = "3.1")
    public String charge(long cents) { return "charged " + cents; }

    /** @deprecated refunds move to RefundClient in version 5 */
    @Deprecated(since = "4.0", forRemoval = true)
    public String refund(long cents) { return "refunded " + cents; }

    public String capture(String key, long cents) { return "captured " + cents + " as " + key; }
}
```

`Checkout` calls `charge(1999)` on line 4 and `refund(500)` on line 5. Before reading on: which
of the two does a plain `javac` report, and does a build with `-Werror` pass?

```text
$ javac Checkout.java
Checkout.java:5: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
        System.out.println(g.refund(500));
                            ^
Note: Checkout.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
1 warning
$ javac -Xlint:deprecation Checkout.java
Checkout.java:4: warning: [deprecation] charge(long) in Gateway has been deprecated
        System.out.println(g.charge(1999));
                            ^
Checkout.java:5: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
        System.out.println(g.refund(500));
                            ^
2 warnings
$ javac -Werror Checkout.java
Checkout.java:5: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
        System.out.println(g.refund(500));
                            ^
error: warnings found and -Werror specified
Note: Checkout.java uses or overrides a deprecated API.
Note: Recompile with -Xlint:deprecation for details.
1 error
1 warning
```

javac reports each use of a `forRemoval` element as a `[removal]` warning; `-Xlint:-removal` turns
them into a note. Uses of plainly deprecated elements are folded into one note per file, and
listed only with `-Xlint:deprecation`. A note isn't a warning, so `-Werror` doesn't count it. A
team that builds with `-Werror` and no `-Xlint` can call `charge` for years without a failed
build. The release that sets `forRemoval = true` fails it at every call, which is what the library
author wants.

The two levels are also suppressed separately. In `Refunds`, one method carries
`@SuppressWarnings("deprecation")` and another is itself `@Deprecated`; both call `charge` and
`refund`. A test double in `TestGateway` overrides `refund`:

```text
$ javac -Xlint:deprecation Refunds.java
Refunds.java:3: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
    static String partial(Gateway g) { return g.charge(700) + ", " + g.refund(300); }
                                                                      ^
Refunds.java:7: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
    static String legacy(Gateway g) { return g.charge(100) + ", " + g.refund(100); }
                                                                     ^
2 warnings
$ javac -Xlint:deprecation TestGateway.java
TestGateway.java:2: warning: [removal] refund(long) in Gateway has been deprecated and marked for removal
    @Override public String refund(long cents) { return "test refund " + cents; }
                            ^
1 warning
```

`"deprecation"` silenced `charge` and not `refund`, which takes `@SuppressWarnings("removal")`. A
deprecated caller is exempt from deprecation warnings but not from removal warnings (JLS §9.6.4.6),
and overriding a for-removal method counts as a use. Unit 7.1.4 shows the JDK's own `[removal]`
warnings on the Security Manager, and a `--release` flag that hides them.

## @SuppressWarnings Covers Its Whole Declaration, Including Warnings Added Later

A service reads its ports from parsed YAML, a tree of maps, lists and scalars. The author cast the
`http` list once, decided the schema made it safe, and suppressed the warning on the method. A
later release added the `admin` line:

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ports {
    /** raw: the parsed YAML, a tree of maps, lists and scalars. */
    @SuppressWarnings("unchecked")
    static Map<String, List<Integer>> ports(Map<String, Object> raw) {
        Map<String, List<Integer>> out = new HashMap<>();
        out.put("http", (List<Integer>) raw.get("http"));     // the YAML schema says: integers
        List admin = (List) raw.get("admin");                 // added in a later release
        out.put("admin", admin);
        return out;
    }

    public static void main(String[] args) {
        Map<String, Object> yaml = Map.of("http", List.of(8080, 8081), "admin", List.of("9090"));
        Map<String, List<Integer>> ports = ports(yaml);
        System.out.println("loaded: " + ports);
        int first = ports.get("admin").get(0);
        System.out.println("admin listens on " + first);
    }
}
```

`javac Ports.java` and `javac -Xlint:unchecked Ports.java` both print nothing. The run:

```text
loaded: {http=[8080, 8081], admin=[9090]}
Exception in thread "main" java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer (java.lang.String and java.lang.Integer are in module java.base of loader 'bootstrap')
	at Ports.main(Ports.java:20)
```

The YAML had the port quoted, so `admin` holds the string `"9090"`. Storing a raw `List` in a
`Map<String, List<Integer>>` is an unchecked conversion, and the map now claims integers it
doesn't hold. That heap pollution fails at line 20, where javac inserted a cast to
`Integer` into a line that has none. The method's annotation covered the new line, because a
suppression applies to everything inside the declaration that carries it, code written after the
annotation included.

`@SuppressWarnings` goes on declarations, not statements, so the narrowest scope is a local
variable declared for the purpose:

```java
@SuppressWarnings("unchecked")
List<Integer> http = (List<Integer>) raw.get("http"); // the YAML schema says: integers
out.put("http", http);
```

Now a plain `javac` prints `Note: Ports.java uses unchecked or unsafe operations.`, and
`-Xlint:unchecked` names the new line, line 13 of the changed file:

```text
Ports.java:13: warning: [unchecked] unchecked method invocation: method put in interface Map is applied to given types
        out.put("admin", admin);
               ^
  required: K,V
  found:    String,List
  where K,V are type-variables:
    K extends Object declared in interface Map
    V extends Object declared in interface Map
Ports.java:13: warning: [unchecked] unchecked conversion
        out.put("admin", admin);
                         ^
  required: V
  found:    List
  where V is a type-variable:
    V extends Object declared in interface Map
2 warnings
```

The suppressed cast is still unchecked: `(List<Integer>)` checks that the object is a `List` and
nothing about its elements. Code that converts untyped input checks each element instead, and then
needs no suppression.

javac silently ignores a key it doesn't know. On a method with an unchecked cast,
`@SuppressWarnings("uncheked")` and `@SuppressWarnings("all")` both left `-Xlint:all` reporting
`[unchecked] unchecked cast`: `"all"` is not a javac 21 key, whatever an IDE does with it.

<!-- practice 1 -->

## @SafeVarargs Removes the Warning at the Cause, and javac Doesn't Check the Promise

A label printer builds batches with a varargs factory, and a retry path wraps one label in a
batch through a generic helper:

```java
public class Labels {
    /** One print job's labels. */
    static final class Batch<T> {
        private final T[] items;
        private Batch(T[] items) { this.items = items; }

        @SafeVarargs
        static <T> Batch<T> of(T... items) { return new Batch<>(items); }

        T[] items() { return items; }
    }

    static <T> Batch<T> single(T item) { return Batch.of(item); }

    static void print(Batch<String> batch) {
        for (String label : batch.items()) System.out.println("label " + label);
    }

    public static void main(String[] args) {
        print(Batch.of("A-17", "B-02"));
        Batch<String> retry = single("C-33");
        System.out.println("retry batch built");
        print(retry);
    }
}
```

```text
label A-17
label B-02
retry batch built
Exception in thread "main" java.lang.ClassCastException: class [Ljava.lang.Object; cannot be cast to class [Ljava.lang.String; ([Ljava.lang.Object; and [Ljava.lang.String; are in module java.base of loader 'bootstrap')
	at Labels.print(Labels.java:16)
	at Labels.main(Labels.java:23)
```

Inside `single`, `T` is erased, so the array javac builds for `Batch.of(item)` is an `Object[]`
(unit 1.3.2 shows why). `Batch` keeps it, `items()` returns it as a `T[]`, and the cast javac
inserted into `print`'s loop fails. The trace names `print` and `main`, not `single`, where the wrong
array was made.

With the annotation replaced by a comment, `-Xlint:all` points at the cause:

```text
Labels.java:8: warning: [unchecked] Possible heap pollution from parameterized vararg type T
        static <T> Batch<T> of(T... items) { return new Batch<>(items); }
                                    ^
  where T is a type-variable:
    T extends Object declared in method <T>of(T...)
Labels.java:13: warning: [unchecked] unchecked generic array creation for varargs parameter of type T[]
    static <T> Batch<T> single(T item) { return Batch.of(item); }
                                                        ^
  where T is a type-variable:
    T extends Object declared in method <T>single(T)
2 warnings
```

With it, a plain `javac` prints nothing, and `-Xlint:all` only this:

```text
Labels.java:8: warning: [varargs] Varargs method could cause heap pollution from non-reifiable varargs parameter items
        static <T> Batch<T> of(T... items) { return new Batch<>(items); }
                                                                ^
1 warning
```

`@SafeVarargs` is the author's statement that the method only reads the elements of its array and
doesn't let the array escape. javac takes it on trust and stops warning every caller, line 13
included. `of` keeps the array and `items()` hands it out, so the statement is false. The
`[varargs]` lint doesn't check it either: it flags any use that passes or returns the array
itself, and element reads, `length` and a for-each loop draw nothing. The fix, a `List<T>` field filled with `List.of(items)` and returned by `items()`, prints
`label C-33` and draws the same `[varargs]` warning. Unit 1.3.2 lists where the annotation is
allowed: constructors and methods that can't be overridden.

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **`@Override` turns a method that stopped overriding into a compile error.** Without it, a
  supertype change or a misspelt name leaves a new method that nothing calls; the check runs only
  when the subclass compiles.
- **A use of a `forRemoval` element warns by default; plain deprecation is a note.** `-Werror`
  ignores the note, `"deprecation"` doesn't suppress `[removal]`, and a deprecated caller is still
  warned about removal.
- **`@SuppressWarnings` covers its whole declaration.** On a method it hides the next unchecked
  line too; on a local variable it hides one cast. javac ignores unknown keys, `"all"` included.
- **`@SafeVarargs` removes the warning at the cause, unchecked.** A method that lets its varargs
  array escape as a `T[]` pollutes the heap, and the `ClassCastException` lands in code with no cast.

## Reference

**The built-in annotations (Java 21, read by reflection):**

| Annotation | Retention | Allowed on |
|---|---|---|
| `@Override` | SOURCE | methods |
| `@Deprecated` | RUNTIME | constructors, fields, local variables, methods, packages, modules, parameters, types |
| `@SuppressWarnings` | SOURCE | no `@Target`: any declaration |
| `@SafeVarargs` | RUNTIME | constructors, methods (static, final or private) |
| `@FunctionalInterface` | RUNTIME | types |

**javac 21 lint keys on this page:**

| Key | Without `-Xlint` | Suppressed by |
|---|---|---|
| `removal` | a warning per use | `@SuppressWarnings("removal")`; `-Xlint:-removal` leaves a note |
| `deprecation` | one note per file | `@SuppressWarnings("deprecation")`; a caller that is itself `@Deprecated` |
| `unchecked` | one note per file | `@SuppressWarnings("unchecked")`; `@SafeVarargs` at call sites |
| `rawtypes` | nothing | `@SuppressWarnings("rawtypes")` |
| `varargs` | nothing | `@SuppressWarnings("varargs")` |

A use inside the outermost class that declares a deprecated element gets no warning of either
kind, even with `-Xlint:all`.

## Challenges

1. Your build passes `-Werror`. Which `-Xlint` keys would you add, and what does each one cost on
   a codebase that has never seen them?
2. A host application loads plugins built against older versions of its API. What would catch a
   plugin method that no longer overrides anything, since the plugin's own `@Override` can't?
3. You maintain a library and want to remove a method in two releases. What do you ship in each
   release, and which of your users' builds fail at which step?
4. Find every `@SuppressWarnings` on a method or class in a codebase you know. How many would
   still be needed on a local variable?

## Design Note: Why javac Trusts @SafeVarargs

Whether a generic varargs method is safe depends on what it does with the array, and the array can
leave through a field, a return value, or a call into code javac isn't compiling. Java 7 chose a
promise over an analysis: the author states it once, only on a method no subclass can override,
and every caller stops warning. The `[varargs]` lint is what remains of the check, and it can't
tell a copy from a leak.

A false promise fails at the first read with the wrong type, in code that did nothing wrong, and
the warning that would have pointed at the cause is the one the annotation removed.

## Code Examples

- Test: [BuiltInAnnotationsTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/annotations/BuiltInAnnotationsTest.java)
- Source: [BuiltInAnnotations.java](src/main/java/com/github/msorkhpar/claudejavatutor/annotations/BuiltInAnnotations.java)
