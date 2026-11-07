# 4.3.1. Introduction to Method References

A rate limiter reads its limit through an `IntSupplier` that is wired once, at startup. A
reviewer shortened the wiring from `() -> settings.limit()` to `settings::limit`, with the comment
"same thing, shorter". Since that change, an operator who reloads the settings sees the new limit,
250, in the log, and the limiter keeps allowing 100 requests a second. Nobody restarted anything.
Where does the 100 come from?

A method reference behaves like a lambda that makes one call, and most of the time the shorter
text means exactly the same thing. This unit is about the places where it doesn't. Every output
below is from Java 21; long stack traces are cut to their first frames.

## A Method Reference Is a Lambda That Makes One Call, and the Target Type Decides What the Call Means

`String::length` names a method without calling it. Like a lambda (unit 4.1.1), it has no type of
its own: the interface the context expects, its target type, supplies the parameters, and the
compiler finds a call to `length` that fits them. As a `Function<String, Integer>` it is
`s -> s.length()`; the kinds of reference and how each one maps the parameters are unit 4.3.2.

A leaderboard wants the best of three scores:

```java
List<Integer> scores = List.of(40, 95, 70);
System.out.println("max(Integer::max)     = " + scores.stream().max(Integer::max).get());
System.out.println("max(Integer::compare) = " + scores.stream().max(Integer::compare).get());
Comparator<Integer> c = Integer::max;
System.out.println("compare(40, 95)       = " + c.compare(40, 95));
```

```text
max(Integer::max)     = 40
max(Integer::compare) = 95
compare(40, 95)       = 95
```

`max` takes a `Comparator<Integer>`, whose method is `int compare(Integer, Integer)`.
`Integer.max(int, int)` takes two ints and returns an int, so the reference compiles, without a
warning. The comparator now is `Integer.max`: asked to compare 40 and 95, it answers 95, a
positive number, which means "40 is greater". With positive scores every comparison says "the
first is greater", so `max` returns whichever score came first.

The compiler checks that the call fits the interface's method: the number of parameters, their
types after boxing, and the return type. It can't check that the method means what the
interface's contract asks for, and a lambda is no safer: `(a, b) -> Math.max(a, b)` compiles too.
The reference hides what the call does, though: a reader sees `max(Integer::max)` and assumes it
finds the maximum.

The leaderboard passes any test whose best score comes first, which is common when test data is
typed in sorted order.

<!-- practice 1 -->

## A Bound Receiver Is Evaluated Once, When the Reference Is Created

`settings::limit` and `() -> settings.limit()` call the same method. They differ in when
`settings` is read. In a reference with an expression before the `::` (a **bound** reference), the
expression is evaluated when the reference is created, and the result is kept. A lambda's body,
receiver included, runs on every call. A counter that issues a new ticket on each `next()` shows
when each one runs:

```java
Counter counter = new Counter();
Supplier<String> ref = counter.next()::toString;
Supplier<String> lambda = () -> counter.next().toString();
System.out.println("issued after creating both: " + counter.issued());
System.out.println("ref:    " + ref.get() + " " + ref.get() + " " + ref.get());
System.out.println("lambda: " + lambda.get() + " " + lambda.get() + " " + lambda.get());
System.out.println("issued at the end: " + counter.issued());
```

```text
issued after creating both: 1
ref:    T-1 T-1 T-1
lambda: T-2 T-3 T-4
issued at the end: 4
```

Creating the reference issued T-1, before anyone called `get()`, and every call returns that
ticket. The lambda issued nothing until it was called, and then a new ticket per call.

The rate limiter has the same shape. `settings` is a `volatile` field that a reload replaces with
a new `Settings` object:

```java
static volatile Settings settings = new Settings(100);

public static void main(String[] args) {
    IntSupplier byRef = settings::limit;
    IntSupplier byLambda = () -> settings.limit();
    settings = new Settings(250);                 // a reload
    System.out.println("byRef:    " + byRef.getAsInt());
    System.out.println("byLambda: " + byLambda.getAsInt());
}
```

```text
byRef:    100
byLambda: 250
```

The reference holds the object that was in the field at startup. The reload changed the field,
not that object, so the limiter reads 100 for as long as the process runs. The lambda reads the
field on each call. A lambda that reads a local copy instead, `Settings s = settings;` and then
`() -> s.limit()`, is pinned just like the reference: what it captured is the object, not the
field (unit 4.1.1).

Neither is wrong in itself: a job that must report the settings it started with wants them
pinned, and the limiter, which must follow reloads, wants the field read on every call.

### A Null Receiver Fails Where the Reference Is Created

Because the receiver is evaluated at creation, a `null` one fails there. Here the settings are not
loaded yet:

```java
static Settings settings;                         // not loaded yet

public static void main(String[] args) {
    IntSupplier byLambda = () -> settings.limit();
    System.out.println("lambda created");
    try {
        IntSupplier byRef = settings::limit;
        System.out.println("ref created");
    } catch (NullPointerException e) {
        System.out.println("creating the ref: " + e);
        System.out.println("\tat " + e.getStackTrace()[0]);
        System.out.println("\tat " + e.getStackTrace()[1]);
    }
    try {
        byLambda.getAsInt();
    } catch (NullPointerException e) {
        System.out.println("calling the lambda: " + e);
        System.out.println("\tat " + e.getStackTrace()[0]);
    }
}
```

```text
lambda created
creating the ref: java.lang.NullPointerException
	at java.base/java.util.Objects.requireNonNull(Objects.java:233)
	at NullReceiver.main(NullReceiver.java:12)
calling the lambda: java.lang.NullPointerException: Cannot invoke "NullReceiver$Settings.limit()" because "NullReceiver.settings" is null
	at NullReceiver.lambda$main$0(NullReceiver.java:9)
```

javac compiles a bound reference as `Objects.requireNonNull(receiver)` followed by the code that
creates the object, so the exception comes from `requireNonNull` and carries no message. The line
number is the only clue. The lambda was created without complaint and failed on its first call,
with a message that names the field.

In the limiter, wiring `settings::limit` before the settings are loaded stops the service at
startup with that bare exception. The lambda version starts, and fails on the first request if the
load never happens. Failing at startup is usually better than failing on a request an hour later,
after the load was quietly skipped.

<!-- practice 2 -->

## javac Points Most Method References Straight at the Method, With No lambda$ Method in Between

Unit 4.1.1 showed that a lambda's body becomes a private method, `lambda$main$0`, and the lambda
itself an `invokedynamic` whose bootstrap method, `LambdaMetafactory.metafactory`, builds an
object that calls that method. A method
reference already names a method, so javac usually passes that one instead. This class has three
plain references, one lambda, and three references of kinds that need help:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class Refs {
    void wire(String prefix) {
        Function<String, Integer> unbound = String::length;
        Function<String, String> bound = prefix::concat;
        Supplier<List<String>> ctor = ArrayList::new;
        Function<String, Integer> lambda = s -> s.length();
        BiFunction<String, Object, String> varargs = String::format;
        IntFunction<int[]> array = int[]::new;
        Supplier<String> viaSuper = super::toString;
    }
}
```

```text
$ javap -p Refs
Compiled from "Refs.java"
public class Refs {
  public Refs();
  void wire(java.lang.String);
  private java.lang.String lambda$wire$3();
  private static int[] lambda$wire$2(int);
  private static java.lang.String lambda$wire$1(java.lang.String, java.lang.Object);
  private static java.lang.Integer lambda$wire$0(java.lang.String);
}
$ javap -v Refs | grep -E "^ +#[0-9]+ REF_"
      #71 REF_invokeVirtual java/lang/String.length:()I
      #73 REF_invokeVirtual java/lang/String.concat:(Ljava/lang/String;)Ljava/lang/String;
      #81 REF_newInvokeSpecial java/util/ArrayList."<init>":()V
      #87 REF_invokeStatic Refs.lambda$wire$0:(Ljava/lang/String;)Ljava/lang/Integer;
      #92 REF_invokeStatic Refs.lambda$wire$1:(Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/String;
      #98 REF_invokeStatic Refs.lambda$wire$2:(I)[I
      #102 REF_invokeVirtual Refs.lambda$wire$3:()Ljava/lang/String;
```

The second listing is the method handle each `invokedynamic` hands to `LambdaMetafactory`.
`String::length`, `prefix::concat` and `ArrayList::new` point at `String.length`, `String.concat`
and the constructor directly. The lambda gets `lambda$wire$0`, and so do the three that need an
adapter:

- `String::format` is `format(String, Object...)`. Called through `BiFunction`, the second
  argument must be wrapped in an `Object[]`, and `lambda$wire$1` does that.
- `int[]::new` has no method to point at: creating an array is a bytecode instruction.
- `super::toString` must call `Object.toString` without virtual dispatch, which only code inside
  `Refs` may do.

A bound receiver needs no adapter. `prefix` is checked with `requireNonNull` and passed as an
argument of the `invokedynamic`, and the function object it returns holds it, the way a lambda holds a
captured value. Performance is not a reason to pick either form: both are an `invokedynamic` and a
small hidden class, and an adapter adds one call to a small private method.

## A Stack Trace Through a Method Reference Has No Frame of Its Own

The missing `lambda$` method shows in stack traces. A list of codes holds a `null`, and a stream
maps each code to its length, once with a reference and once with a lambda. `show` runs the
code and prints the exception and its first frames:

```java
List<String> codes = Arrays.asList("A1", null, "C3");
show("ref", () -> codes.stream().map(String::length).toList());
show("lambda", () -> codes.stream().map(s -> s.length()).toList());
```

```text
ref: java.lang.NullPointerException
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:1024)
	at java.base/java.util.stream.AbstractPipeline.copyInto(AbstractPipeline.java:509)
lambda: java.lang.NullPointerException: Cannot invoke "String.length()" because "<parameter1>" is null
	at Traces.lambda$main$1(Traces.java:12)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.Spliterators$ArraySpliterator.forEachRemaining(Spliterators.java:1024)
```

The reference's NPE was thrown in the metafactory's hidden class, before `String.length` was
entered, and the JVM leaves hidden frames out of stack traces. So the top frame is the stream's,
no frame names our code, and the NPE has no message: the JVM computes that message from the
method that threw, and here that method is hidden. The lambda's trace starts at `lambda$main$1`,
line 12, with the message.

When the referenced method is our own, it is in the trace, and the reference adds nothing between
it and the stream. `parse(code)` is `Integer.parseInt(code.substring(1))`, and here
`map(Traces::parse)` and `map(s -> parse(s))` fail on the code `"Bx"` (in a `List.of`, hence the
other spliterator):

```text
ref to parse: java.lang.NumberFormatException: For input string: "x"
	... 3 java.lang frames
	at Traces.parse(Traces.java:6)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
	at java.base/java.util.AbstractList$RandomAccessSpliterator.forEachRemaining(AbstractList.java:722)
lambda calling parse: java.lang.NumberFormatException: For input string: "x"
	... 3 java.lang frames
	at Traces.parse(Traces.java:6)
	at Traces.lambda$main$4(Traces.java:15)
	at java.base/java.util.stream.ReferencePipeline$3$1.accept(ReferencePipeline.java:197)
```

Neither trace gives the line where the reference was written. In a pipeline with five
`map(SomeType::method)` steps, a bare NPE from a reference to a JDK method still tells you which
step failed if you count: each `map` stage calls the next one's `accept`, so a failure in the
third step sits on three `ReferencePipeline$3$1.accept` frames. It doesn't tell you the line, or
what was null.

> **Hidden frames.** `-XX:+UnlockDiagnosticVMOptions -XX:+ShowHiddenFrames` puts the generated
> frame back (`Traces$$Lambda/0x….apply(Unknown Source)`), and the NPE gets its message again. It
> is a diagnostic option for a debugging session.

<!-- practice 3 -->

## A Lambda Reads Better When the Call Needs Swapped Arguments, a Constant, or a Chain

A method reference passes the interface's parameters to one method, in order. When the call needs
anything else, the reference form needs a helper, and the helper moves the logic away from the
call site:

```java
Comparator<String> newestFirst = (a, b) -> b.compareTo(a);     // arguments swapped
Predicate<String> isError = line -> line.startsWith("ERROR");    // a constant argument
Function<Order, String> city = order -> order.customer().city(); // two calls

// The same three as method references:
Comparator<String> newestFirstRef = Reads::newestFirst;
Predicate<String> isErrorRef = Reads::isError;
Function<Order, String> cityRef = Reads::cityOf;
```

`String::startsWith` alone does not fit a `Predicate<String>`: it needs a prefix that the
predicate's one parameter doesn't supply.

```text
NoRef.java:4: error: incompatible types: invalid method reference
    Predicate<String> isError = String::startsWith;
                                ^
    unexpected instance method startsWith(String) found in unbound lookup
```

The helpers make the reader jump to learn that one swaps its arguments, one checks a prefix and
one walks two getters; the lambda says it in place. A named method is worth it when the logic has
a name in the domain, is reused, or needs its own test (unit 4.1.4). For descending order,
`Comparator.reverseOrder()` beats both. Prefer the reference for a straight pass-through
(`map(Order::customer)`, `forEach(System.out::println)`), and the lambda when the call has to
rearrange, add or chain anything.

## Summary

- **A method reference is a lambda that makes one call; the target type decides what it means.**
  javac checks the call's shape, not its sense: `max(Integer::max)` compiles and returns the first
  element.
- **A bound receiver is evaluated once, when the reference is created.** `settings::limit` keeps
  the old settings after a reload, and a `null` receiver throws a message-less NPE at creation;
  `() -> settings.limit()` reads the field on every call and fails only when called.
- **javac points most method references straight at the method.** Of the forms above, only
  varargs, array constructors and `super::` got a `lambda$` adapter; a bound receiver is passed as
  a captured argument.
- **A stack trace through a method reference has no frame of its own.** A reference to a JDK
  method that meets a null element gives a trace with no line of ours and no NPE message.
- **A lambda reads better when the call swaps arguments, adds a constant or chains calls.** Use the
  reference for a straight pass-through.

## Reference

**When the receiver is read (Java 21, `ex/eval`):**

| Form | Receiver evaluated | `null` receiver |
|---|---|---|
| `expr::m` (bound) | once, when the reference is created | `NullPointerException` at creation, from `Objects.requireNonNull`, no message |
| `() -> expr.m()` | on every call | `NullPointerException` at the call, with a helpful message |

**What javac 21 generates (`javap -p`, `javap -v`):**

| Written | `lambda$` method | Method handle passed to `LambdaMetafactory` |
|---|---|---|
| `String::length` | none | `REF_invokeVirtual String.length` |
| `prefix::concat` | none | `REF_invokeVirtual String.concat`; `prefix` is an `invokedynamic` argument |
| `ArrayList::new` | none | `REF_newInvokeSpecial ArrayList.<init>` |
| `s -> s.length()` | one | `REF_invokeStatic lambda$…` |
| `String::format` as `BiFunction<String, Object, String>` (varargs) | one | `REF_invokeStatic lambda$…` |
| `int[]::new` | one | `REF_invokeStatic lambda$…` |
| `super::toString` | one, an instance method | `REF_invokeVirtual lambda$…` |

## Challenges

1. Your service wires twenty components with bound references to fields that a reload replaces.
   How would you find them all, and which ones should stay bound?
2. A production NPE has no message and no frame of your code, and the pipeline has six
   `map(Type::method)` steps. What can you learn from the trace alone, and what would you change,
   in the code or in a test environment's JVM flags, to find the step?
3. `Integer::max` compiles as a `Comparator<Integer>`. Find two other JDK methods that fit
   `Comparator` or `Predicate` by shape and mean something else. Would a code review catch them?

## Design Note: Why a Bound Reference Evaluates Its Receiver Early

The Java Language Specification (section 15.13.3) has the receiver evaluated when the method
reference expression is evaluated, and a `null` result throws there. Re-evaluating it on every
call would make `expr::m` a lambda in all but syntax, and `counter.next()::m` would run `next()`
once per call, hidden behind text that looks like a name. Early evaluation makes a bound reference
a value: an object paired with one of its methods, fixed when made, like a lambda's captured
variables.

This is the bug in the opening. Two spellings that look interchangeable read the receiver at
different times, and a reviewer or an automated refactoring that shortens a lambda to a reference can
change when a value is read without changing anything else on the line.

## Code Examples

- Source: [MethodReferenceIntro.java](src/main/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceIntro.java)
- Test: [MethodReferenceIntroTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/methodreferences/MethodReferenceIntroTest.java)
