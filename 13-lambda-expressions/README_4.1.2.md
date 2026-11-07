# 4.1.2. Syntax and Structure of Lambda Expressions

A shared messaging library adds a second way to register a handler, so that handlers can return
an acknowledgement:

```java
void onMessage(Consumer<Message> handler)      { ... }   // existing
void onMessage(Function<Message, Ack> handler) { ... }   // new
```

Three services upgrade and fail to compile on lines nobody changed:

```text
Bus.java:17: error: reference to onMessage is ambiguous
        bus.onMessage(m -> audit.add(m.body()));
           ^
  both method onMessage(Consumer<Message>) in Bus and method onMessage(Function<Message,Ack>) in Bus match
```

`audit.add` returns a `boolean`, not an `Ack`. Why can't the compiler tell which overload fits?
How a lambda gets its target type is unit 4.1.1; every output here is from Java 21.

## A Lambda's Parameters Are All Inferred or All Declared

```java
Supplier<String> none        = () -> "ready";
UnaryOperator<String> one    = s -> s.strip();                          // parentheses optional
BinaryOperator<Integer> inf  = (a, b) -> a + b;                         // inferred (implicitly typed)
BinaryOperator<Integer> exp  = (Integer a, Integer b) -> a + b;         // types declared
BinaryOperator<Integer> vars = (final var a, @Trimmed var b) -> a + b;  // var; @Trimmed is our own
UnaryOperator<String> plainVar = (var s) -> s.strip();
```

One list can't mix the forms:

```text
Mixed.java:4: error: invalid lambda parameter declaration
    BinaryOperator<Integer> typedAndBare = (Integer a, b) -> a + b;
                                           ^
  (cannot mix implicitly-typed and explicitly-typed parameters)
Mixed.java:5: error: invalid lambda parameter declaration
    BinaryOperator<Integer> varAndTyped  = (var a, Integer b) -> a + b;
                                           ^
  (cannot mix 'var' and explicitly-typed parameters)
```

An annotation or `final` needs a declared type or `var`: `(@Trimmed s) -> s` and `(final s) -> s`
are syntax errors. `var` exists to allow them, and still counts as inferred.

> **Java 22 differs.** `(s, _) -> s` marks an unused parameter. Java 21 rejects it ("unnamed
> variables are a preview feature and are disabled by default"); it is standard from Java 22.

## A Body's Shape Decides Whether It Returns a Value, Nothing, or Either

An expression body returns its value. A `void` target accepts only a statement expression (a
method call, an assignment, `++`, `new Foo()`) and discards its value. So a call like `seen.add(...)` fits both kinds of target:

```java
Runnable asRunnable       = () -> seen.add("a");      // the boolean is discarded
Callable<Boolean> asValue = () -> seen.add("b");      // the boolean is returned
```

```text
call() = true, seen = [a, b]
```

A block body is void-compatible when no `return` in it carries a value, and value-compatible when
every `return` carries one and control can't reach the closing brace. javac names each mismatch:

```java
Runnable notAStatement   = () -> count + 1;
Callable<Integer> noRet  = () -> { count++; };
Function<String, Integer> somePaths = s -> {
    if (s.isEmpty()) return 0;
    if (s.length() > 3) return s.length();
};
Runnable returnsValue    = () -> { return count; };
```

```text
Incompatible.java:7: error: incompatible types: lambda body is not compatible with a void functional interface
    Runnable notAStatement   = () -> count + 1;
                                           ^
    (consider using a block lambda body, or use a statement expression instead)
Incompatible.java:8: error: incompatible types: bad return type in lambda expression
    Callable<Integer> noRet  = () -> { count++; };
                               ^
    missing return value
Incompatible.java:9: error: incompatible types: bad return type in lambda expression
    Function<String, Integer> somePaths = s -> {
                                          ^
    missing return value
Incompatible.java:13: error: incompatible types: bad return type in lambda expression
    Runnable returnsValue    = () -> { return count; };
                                              ^
    unexpected return value
```

`somePaths` falls off its end for a two-letter string. A block that can't reach its end and has no
`return`, because it throws or loops forever, is both void- and value-compatible.

## When a Body Fits Both, the Overload That Returns a Value Wins

`ExecutorService.submit` is overloaded for `Runnable` and `Callable`; `pick` copies that pair.
Which overload does each call choose?

```java
static void pick(String body, Runnable r)        { System.out.printf("%-38s -> Runnable%n", body); }
static <T> void pick(String body, Callable<T> c) { System.out.printf("%-38s -> Callable%n", body); }

pick("seen.add(\"x\")", () -> seen.add("x"));
pick("{ seen.add(\"x\"); }", () -> { seen.add("x"); });
pick("System.out.println()", () -> System.out.println());
pick("{ throw new IllegalStateException(); }", () -> { throw new IllegalStateException(); });
pick("{ while (true) { } }", () -> { while (true) { } });
```

```text
seen.add("x")                          -> Callable
{ seen.add("x"); }                     -> Runnable
System.out.println()                   -> Runnable
{ throw new IllegalStateException(); } -> Callable
{ while (true) { } }                   -> Callable
```

When a body fits both and its parameter types are known (declared, or there are none, as here),
the compiler prefers the interface whose method returns a value. `submit` does the same:

```java
pool.submit(() -> seen.add("o-7"))
pool.submit(() -> { seen.add("o-8"); })
pool.submit(() -> { archive("o-9"); return null; })   // archive(...) throws IOException
```

```text
seen.add("o-7")                   get() = true
{ seen.add("o-8"); }              get() = null
{ archive("o-9"); return null; }  get() threw, cause = java.io.IOException: disk full while archiving o-9
```

Adding braces turned a `Future` holding `true` into one holding `null`. The third compiles only because `return null` makes it a `Callable`, whose
`call()` may throw `Exception`. Without it javac picks `Runnable` and reports `unreported
exception IOException; must be caught or declared to be thrown`. Catching it inside the lambda
instead hides the failure or puts another exception in the `Future` (unit 3.2.2).

<!-- practice 1 -->

## An Implicitly Typed Lambda Can't Choose Between Overloads of the Same Arity

Back to the library. With both `onMessage` overloads:

```java
bus.onMessage(m -> audit.add(m.body()));       // ambiguous
bus.onMessage(m -> System.out.println(m));     // ambiguous
bus.onMessage(m -> ack(m));                    // ambiguous
bus.onMessage(m -> { audit.add(m.body()); });  // Consumer
bus.onMessage(m -> { return ack(m); });        // Function
```

`m`'s type depends on the overload being chosen, so for an implicitly typed lambda javac checks
only the body's shape. An expression body
might return a value, so it fits both, even `System.out.println(m)`. A block rules one out. Without declared types the value-returning preference doesn't apply, so the call is ambiguous. `(var m) -> ack(m)` is ambiguous too.

```java
bus.onMessage((Message m) -> audit.add(m.body()));            // Consumer overload
bus.onMessage((Message m) -> ack(m));                         // Function overload
bus.onMessage((Consumer<Message>) m -> audit.add(m.body()));  // Consumer overload
bus.onMessageAck(m -> ack(m));                                // onMessageAck
```

Declared types let the compiler check the body: a `boolean` isn't an `Ack`, so only `Consumer`
fits the first; `ack(m)` fits both, and the value-returning one wins. A cast names the target directly.
These fix one call site; only a new name fixes every caller. The JDK's `comparingInt` and `mapToInt` are separate names: a replica that
overloads `by(ToIntFunction)` and `by(Function)` makes `by(s -> s.length())` ambiguous.

A cast can also name several types, giving a lambda both interfaces:

```java
Runnable plain = () -> System.out.println("tick");
Runnable both  = (Runnable & Serializable) () -> System.out.println("tick");
// each written to an ObjectOutputStream:
```

```text
plain: java.io.NotSerializableException
both:  written
```

## A Lambda Can't Implement a Generic Method

```java
interface Picker {
    <T extends Comparable<T>> T pick(T a, T b);
}
```

```text
Generic.java:6: error: incompatible types: invalid functional descriptor for lambda expression
    Picker larger = (a, b) -> a.compareTo(b) >= 0 ? a : b;
                    ^
    method <T>(T,T)T in interface Picker is generic
  where T is a type-variable:
    T extends Comparable<T> declared in method <T>pick(T,T)
```

A lambda has nowhere to declare `<T>`. A method reference to a generic method fits, and so does an
anonymous class:

```java
static <T extends Comparable<T>> T larger(T a, T b) { return a.compareTo(b) >= 0 ? a : b; }

Picker byRef = GenericRef::larger;   // byRef.pick("pear", "fig") is "pear", byRef.pick(3, 11) is 11
```

If each caller needs only one type, make the interface generic (`PickerOf<T>`) and a lambda fits.

<!-- practice 2 -->

## Summary

- **Parameters are all inferred or all declared.** `var` allows annotations and `final`, but to
  overload resolution it is still inferred.
- **A body's shape decides what it can return.** A statement expression that has a value fits both
  void and value targets; a block that can reach its end returns nothing.
- **When a body fits both, the value-returning overload wins**, so braces can turn
  `submit(...).get()` from a value into `null`.
- **An implicitly typed lambda can't choose between same-arity overloads.** Declared types or a
  cast fix one call; only a distinct method name fixes every caller.
- **A lambda can't implement a generic method.** Use a method reference, a class, or a generic
  interface.

## Reference

| Body | Fits a `void` method | Fits a value method |
|---|---|---|
| `x + 1` (not a statement) | no | yes |
| `list.add(x)`, `i++`, `new Foo()` | yes | yes |
| `System.out.println(x)` (a `void` call) | yes | no |
| `{ ...; }`, end reachable, no `return v` | yes | no |
| `{ ... return v; }`, end unreachable | no | yes |
| `{ throw ...; }`, `{ while (true) { } }` | yes | yes |

While overloads are chosen for an inferred lambda, every expression body counts as fitting both.

## Challenges

1. Find an API in your codebase that overloads one name on two functional interfaces of the same
   arity. Which call sites compile only because of a block body or declared types?
2. You must add a value-returning variant to a published `register(Consumer<Event>)`. Compare a new
   name with a new handler interface.
3. A team rule says "always braces in lambdas". What does it change at run time?

## Design Note: Overloads Are Chosen Before Inferred Bodies Are Checked

For an implicitly typed lambda, javac picks the overload from the arity and the shape of the body,
and only then types the body, once, against one target. Typing it against every overload, with
each candidate's parameter types, would multiply the work for nested lambdas, and an edit deep in
a body could move the call to another method, which already happens for declared types. The price
is this unit's ambiguity, and it falls on API designers.

## Code Examples

- Test: [LambdaSyntaxTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaSyntaxTest.java)
- Source: [LambdaSyntax.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaSyntax.java)