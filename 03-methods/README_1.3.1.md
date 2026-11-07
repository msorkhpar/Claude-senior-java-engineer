# 1.3.1 Method Declaration and Invocation in Java

One release of the export service brought three tickets. CSV exports arrive named `report.dat`.
With debug logging off, every order is still rendered for a log line nobody writes, and while
auditing is off the audit sequence numbers keep being used up. A support thread with 12,000 replies
fails with `StackOverflowError` for a few minutes after every deploy, then loads. Parameters,
argument passing and overloading are units 1.3.2 to 1.3.4.

## An Instance Method Is Chosen by the Object; a Static Method by the Declared Type

Both exporters declare a static `extension()` and an instance `format()`:

```java
class Exporter {
    static String extension() { return "dat"; }
    String format() { return "generic"; }
}

class CsvExporter extends Exporter {
    static String extension() { return "csv"; }
    @Override String format() { return "csv"; }
}
```

Predict the four lines:

```java
Exporter exporter = new CsvExporter();
System.out.println("format():    " + exporter.format());
System.out.println("extension(): " + exporter.extension());

Exporter none = null;
System.out.println("extension() through null: " + none.extension());
System.out.println("format() through null: " + none.format());
```

Compiled with `-g`, which names the variable in the message:

```text
format():    csv
extension(): dat
extension() through null: dat
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "Exporter.format()" because "none" is null
```

An instance call's implementation is chosen when it runs: the JVM looks at the object's class and
runs its override, so `format()` says `csv`. A static call is resolved by the compiler, from the
type of the expression left of the dot. That expression is still evaluated, but its value is
thrown away, which is why the call through `null` works. `javap -c` shows the two instructions:

```text
      12: invokevirtual #16                 // Method Exporter.format:()Ljava/lang/String;
      28: invokestatic  #32                 // Method Exporter.extension:()Ljava/lang/String;
```

`CsvExporter.extension()` *hides* `Exporter.extension()`: it runs only when the call names
`CsvExporter` or an expression of that type. The compiler can tell you: `@Override` on a static
method fails with `static methods cannot be annotated with @Override`, and `javac -Xlint:static`
warns about every static call made through an expression.

The export service built names as `base + "." + exporter.extension()` with `exporter` typed as
`Exporter`, so every format got `.dat`. The call through `null` is a latent bug: it works until
someone turns the method into an instance method, and then a null that was always there throws.

<!-- practice 1 -->

## A Private Method Is Never Overridden, So the Class's Own Calls Keep Using It

An importer checks each row in a private method. A stricter importer declares its own `validate`:

```java
class Importer {
    void run(String row) {
        System.out.println(getClass().getSimpleName() + " accepts \"" + row + "\": " + validate(row));
    }
    private boolean validate(String row) { return !row.isEmpty(); }
}

class StrictImporter extends Importer {
    boolean validate(String row) { return row.matches("\\d+(,\\d+)*"); }
}
```

`new StrictImporter().run("12,x,7")` prints:

```text
StrictImporter accepts "12,x,7": true
```

A private method is not inherited, so `StrictImporter.validate` is a new method that shares a
name. `Importer.run` calls its own private method whatever the object is, and rows the strict
importer exists to reject go in. With `@Override` on `StrictImporter.validate` the mistake
doesn't compile (`method does not override or implement a method from a supertype`). Annotate
every method you mean as an override, and make the hook `protected` or package-private.

<!-- practice 4 -->

## Every Argument Is Evaluated, Left to Right, Before the Method Runs

As with operands (unit 1.1.4), Java evaluates the expression before the dot, then each argument
in order, and only then checks the receiver for `null` and enters the method. Debug is off and
`debug` checks the level itself, `audit()` returns `null` because auditing is off, and `render`
and `nextSeq` print when they run:

```java
log.debug("placing " + render("A-7"));            // debug(String)
log.debug(() -> "placing " + render("A-7"));      // debug(Supplier<String>)
audit().record(nextSeq(), render("A-7"));         // audit() returns null
```

The program prints a header before each call, catches the exception, then takes the next number:

```text
debug(String):
  render(A-7)
debug(Supplier):
audit().record(...):
  audit()
  nextSeq() -> 42
  render(A-7)
  Cannot invoke "Audit.record(long, String)" because the return value of "Orders.audit()" is null
the next record:
  nextSeq() -> 43
```

`debug(String)` never used its argument, but the order was rendered before `debug` started. A logging placeholder (`"placing {}", render(order)`) defers only the
formatting: `render` still runs. A `Supplier` lets the method decide whether the work happens.
The same holds for any "default" argument, such as `Optional.orElse(...)` or
`Map.getOrDefault(key, ...)`: it is computed whether or not it is needed.

The null receiver threw only after both arguments ran, so sequence number 42 went to a record that
was never written. Check a receiver that can be `null` before computing arguments with side
effects.

<!-- practice 2 -->

## Each Call Holds a Stack Frame Until It Returns, So Recursion Depth Is Limited by the Stack

A call's frame, with its parameters and locals, stays on the stack until the method returns, so
recursion keeps the whole chain's frames alive. The support page renders replies recursively, and
the failing thread is a chain of 12,000 replies, each answering the one before:

```java
static void render(Reply r, int depth) {
    lines++;                                       // stands in for appending one indented line
    for (Reply child : r.replies) render(child, depth + 1);
}
```

We render it right after the JVM starts, then again after rendering 20,000 short discussions has
warmed `render` up, on the default 1 MB stack:

```text
just after start: StackOverflowError after 11112 lines
after warm-up   : rendered 12000 lines
```

Two more runs failed after 10,203 and 10,972 lines. The limit is bytes of stack. A frame of the interpreted method is larger than the frame of the same method once the JIT
has compiled it, and when that happens varies from run to run. So the thread fails after each
deploy and loads once the server is warm.

`-Xss2m` made the first attempt succeed too, but it only moves the limit, for every thread that
doesn't set its own. HotSpot doesn't eliminate tail calls either: `return count(n - 1, acc + 1);`
still overflowed at 1,000,000. For data whose depth users control, keep the pending work on the
heap:

```java
static void renderIteratively(Reply root) {
    Deque<Reply> pending = new ArrayDeque<>();
    pending.push(root);
    while (!pending.isEmpty()) {
        Reply r = pending.pop();
        lines++;
        for (int i = r.replies.size() - 1; i >= 0; i--) pending.push(r.replies.get(i));
    }
}
```

```text
iterative, 1,000,000 deep: rendered 1000000 lines
```

The children are pushed last first, so they come off in the recursion's order. This version
leaves out the depth, which the stack would have to carry too.

<!-- practice 3 -->

## Summary

- **An instance method is chosen by the object's class at run time; a static method by the
  declared type at compile time.** A subclass's static method hides the parent's, and a static
  call through `null` runs.
- **A private method is never overridden.** The class's own calls keep using it; `@Override`
  turns the mistake into a compile error.
- **Every argument is evaluated, left to right, before the method runs**, even when the method
  ignores it and even when the receiver turns out to be `null`.
- **Recursion depth is limited by stack bytes, which change as the JIT compiles.** Recursion over
  data whose depth users control needs an explicit stack.

## Reference

**Which method a call runs (Java 21):**

| Method | Chosen by | A subclass method with the same signature |
|---|---|---|
| instance, not `private`, not `final` | the object's class, at run time | overrides it if instance; doesn't compile if static: `overriding method is static` |
| `final` instance | the declaring class | doesn't compile: `overridden method is final` |
| `private` | the declaring class, at compile time | is a separate method |
| `static` | the type of the expression left of the dot, at compile time | hides it if static; doesn't compile if instance: `overridden method is static` |

**The stack (Java 21, HotSpot, Linux x64):**

| Fact | Value |
|---|---|
| default thread stack | 1 MB (`ThreadStackSize = 1024`), set with `-Xss` |
| one thread's stack | `new Thread(group, task, name, stackSize)`; the javadoc allows a platform to ignore it |
| `StackOverflowError` | an `Error`; the thread can catch it and go on, but state the overflow cut short may be inconsistent |
| tail calls | not eliminated |

## Challenges

1. Compile your codebase with `-Xlint:static`. Which flagged calls would change behaviour if
   the method became an instance method?
2. Your logger has `debug(String, Object...)`. Which call sites still pay for their arguments with
   debug off?
3. A JSON parser recurses once per nesting level. What depth do you accept from an untrusted
   client, and do you enforce it with a counter or by catching `StackOverflowError`?

## Design Note: Why a Static Call Is Bound at Compile Time

A static method has no receiver, so when it runs there is no object whose class could pick an
override. Java resolves the call against the type the compiler sees, which is cheap, and leaves
per-type behaviour to objects. The cost comes when a static utility, such as a clock, later needs
to vary. It must become an instance method on an object that callers receive, and every caller
changes. That is why code meant to be tested takes a `java.time.Clock` instead of calling
`System.currentTimeMillis()`.

## Code Examples

- Test: [MethodDeclarationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methods/MethodDeclarationTest.java)
- Source: [MethodDeclaration.java](src/main/java/com/github/msorkhpar/claudejavatutor/methods/MethodDeclaration.java)
