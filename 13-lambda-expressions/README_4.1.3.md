# 4.1.3. Using Lambda Expressions with Functional Interfaces

A pricing service passes `Function<Integer, Integer>` fee steps through a helper
`mapAll(List<T>, Function<T, R>)` and sorts its support tickets with a `Comparator` that ends in
`.reversed()`. In one week a teammate's `Function<Object, String>` won't compile against the
helper, the ticket dashboard lists its queues backwards, and a profiler shows 300 MB of `Integer`s
per batch. Each is a choice of functional interface, or of how two of them are combined.

## One Abstract Method Makes an Interface Functional; @FunctionalInterface Only Checks It

A lambda can target any interface with exactly one abstract method left once `default` and
`static` methods, and abstract redeclarations of `Object`'s public methods, are set aside.
`Comparator` declares two abstract methods:

```java
System.out.println(c.getSimpleName() + " abstract methods: " + Arrays.stream(c.getDeclaredMethods())
        .filter(m -> Modifier.isAbstract(m.getModifiers()))
        .map(Method::getName).sorted().toList());
```

```text
Comparator abstract methods: [compare, equals]
```

`equals(Object)` doesn't count: every implementation inherits one from `Object`, so a lambda
never has to supply it. `compare` is the only method left, and `Comparator` is functional.

An unannotated one-method `RateLimit` takes a lambda just as well; the annotation changes where a
mistake is reported. A teammate adds `void reset(String clientId)` to it. Without the annotation
`RateLimit.java` still compiles, and the error appears in whichever file holds a lambda:

```text
Gateway.java:2: error: incompatible types: RateLimit is not a functional interface
    RateLimit open = clientId -> true;
                     ^
    multiple non-overriding abstract methods found in interface RateLimit
```

With `@FunctionalInterface`, the interface itself fails, in the change that broke it:

```text
RateLimit.java:1: error: Unexpected @FunctionalInterface annotation
@FunctionalInterface
^
  RateLimit is not a functional interface
    multiple non-overriding abstract methods found in interface RateLimit
```

Put it on every interface you publish for lambdas.

## Use java.util.function Until a Name, a Checked Exception or a Third Parameter Asks for Your Own

The JDK's interfaces come with `andThen`, `and` and `negate`, and the streams and collections
APIs take them. Declare your own when one of three things is true:

- the name carries meaning a reviewer needs: `FeeRule` says more than
  `BiFunction<Order, Customer, Money>`;
- the method must throw a checked exception, which `Function.apply` can't declare (unit 3.2.2);
- it needs three or more parameters: the JDK stops at `BiFunction`.

A `Parser` with the same shape as `Function<String, Integer>` is not one:

```java
class Parsers {
    @FunctionalInterface
    interface Parser { Integer parse(String text); }

    static Parser decimal = Integer::valueOf;
    static Function<String, Integer> asFunction = decimal;           // does not compile
}
```

```text
Parsers.java:8: error: incompatible types: Parser cannot be converted to Function<String,Integer>
```

A lambda fits either type, but once it is typed as a `Parser` it is not a `Function`: a cast
compiles and throws `ClassCastException`. Every boundary needs an adapter (`decimal::parse`): `Function.andThen` won't take a `Parser`
either, and `Parser` has no `andThen` until you write it.

## Function<Integer, Integer> Boxes Each Value Unless the JIT Removes It; IntUnaryOperator Never Boxes

`Function<Integer, Integer>.apply` takes and returns objects, so `f.apply(i)` boxes `i` and the
result. `IntUnaryOperator.applyAsInt` takes and returns `int`. We summed `f.apply(i)` for ten
million `i` in a program, `Boxing`, and read the thread's allocation counter
(`com.sun.management.ThreadMXBean.getThreadAllocatedBytes`) around each loop:

```java
static long boxed(Function<Integer, Integer> f) {
    long sum = 0;
    for (int i = 0; i < N; i++) sum += f.apply(i);
    return sum;
}
```

`primitive(IntUnaryOperator f)` is the same loop with `applyAsInt`. Round 5, after four warm-ups,
first with one lambda passed to each method, then with three (one row of three shown):

```text
## one lambda through each method
round 5, lambda 1   Function<Integer,Integer>:      28 KB   3 ms   IntUnaryOperator:   0 KB   2 ms
## three lambdas through each method
round 5, lambda 1   Function<Integer,Integer>: 312,496 KB  35 ms   IntUnaryOperator:   0 KB  14 ms
```

With one lambda, the JIT inlined it, saw that no `Integer` escaped, and removed the boxing. With
three, the call site was megamorphic and was not inlined, so every call allocated two 16-byte
`Integer`s (the `Integer` cache covers only -128 to 127). A shared pipeline that runs many
lambdas through one call site is the second case. The timings are from one machine, not JMH.
The Reference lists the specialisations for `int`; `long` and `double` have the same set.

## andThen Runs the Receiver First and compose the Argument First; and/or Stop Early

An order of 100.00, in cents, gets 10 % off and 5.00 shipping. Which call gives
95.00?

```java
Function<Integer, Integer> discount = cents -> cents * 90 / 100;   // 10 % off
Function<Integer, Integer> shipping = cents -> cents + 500;        // flat 5.00

System.out.println("discount.andThen(shipping): " + discount.andThen(shipping).apply(10_000));
System.out.println("discount.compose(shipping): " + discount.compose(shipping).apply(10_000));
```

```text
discount.andThen(shipping): 9500
discount.compose(shipping): 9450
```

`f.andThen(g)` is `g(f(x))`; `f.compose(g)` is `f(g(x))`, so the second discounted the shipping
too.

`Predicate.and` and `or` behave like `&&` and `||`: the argument runs only if the receiver hasn't
decided the answer. That makes the order a null guard:

```java
Predicate<String> present = code -> code != null;
Predicate<String> domestic = code -> code.startsWith("DE-");
List<String> codes = Arrays.asList("DE-10115", null, "FR-75001");

System.out.println("present.and(domestic): " + codes.stream().filter(present.and(domestic)).toList());
System.out.println("domestic.and(present): " + codes.stream().filter(domestic.and(present)).toList());
```

```text
present.and(domestic): [DE-10115]
Exception in thread "main" java.lang.NullPointerException: Cannot invoke "String.startsWith(String)" because "code" is null
```

<!-- practice 1 -->

## reversed() Reverses the Whole Comparator Chain Built Before It

Support tickets are listed by queue A to Z, and within a queue by priority, highest first:

```java
Comparator<Ticket> wanted = Comparator.comparing(Ticket::queue)
        .thenComparing(Ticket::priority, Comparator.reverseOrder());
Comparator<Ticket> chainReversed = Comparator.comparing(Ticket::queue)
        .thenComparing(Ticket::priority)
        .reversed();
```

```text
thenComparing(priority, reverseOrder()): [access/3/T-7, access/1/T-9, billing/3/T-2, billing/1/T-4]
thenComparing(priority).reversed():      [billing/3/T-2, billing/1/T-4, access/3/T-7, access/1/T-9]
```

`reversed()` wraps everything chained before it, so the queues came out Z to A too. Reverse one key by passing
`Comparator.reverseOrder()` to its `thenComparing`, or by reversing the key's own comparator
before you chain it.

> **Inference stops at the chain.** `Comparator.comparing(t -> t.priority()).reversed()` does
> not compile: `comparing` gets no target type when `reversed()` is called on it, so `t` is an
> `Object` (`cannot find symbol ... variable t of type Object`). An exact method reference
> (`Ticket::priority`) or a typed parameter (`(Ticket t) -> ...`) fixes it; a reference to an
> overloaded method does not (unit 4.3.3).

<!-- practice 2 -->

## Accept Function<? super T, ? extends R>, or Callers' Functions Don't Fit

A function over `Object` can label `Integer`s, but the compiler rejects it. The helper from the
opening:

```java
static <T, R> List<R> mapAll(List<T> items, Function<T, R> f) {
```

rejects a stored `Function<Object, String> describe` for a `List<Integer>`:

```text
Labels.java:15: error: method mapAll in class Labels cannot be applied to given types;
        return mapAll(ids, describe);                                    // does not compile
               ^
  required: List<T>,Function<T,R>
  found:    List<Integer>,Function<Object,String>
  reason: inference variable T has incompatible equality constraints Object,Integer
```

`T` must be `Integer` for the list and `Object` for the function, and a `Function<Object, String>`
is not a `Function<Integer, String>`: generic types are invariant. A function only consumes `T`
and only produces `R`, so the parameter should be `Function<? super T, ? extends R>`. With that
signature the same call compiles and prints `[Integer:7, Integer:42]`. An inline lambda hides the
problem, because it is typed to fit; stored functions fail. The JDK's own
signatures use these wildcards: `Stream.map`, `Function.andThen`, `Comparator.comparing`.

<!-- practice 3 -->

## Summary

- **One abstract method makes an interface functional**; `@FunctionalInterface` reports a second
  one at the interface.
- **Prefer `java.util.function`**; your own interface needs adapters at every boundary.
- **`Function<Integer, Integer>` allocates two `Integer`s per call** wherever the JIT can't inline
  the lambda.
- **`andThen` runs the receiver first; `and`/`or` stop early**, so the guard goes first.
- **`reversed()` reverses the whole chain.**
- **Take `Function<? super T, ? extends R>`**, or callers' functions don't fit.

## Reference

| Method | Result |
|---|---|
| `f.andThen(g)` / `f.compose(g)` | `g(f(x))` / `f(g(x))` |
| `p.and(q)` / `p.or(q)` | `q` runs only if `p` is `true` / `false` |
| `p.negate()`, `Predicate.not(p)` | the opposite of `p` |
| `c.thenComparing(key, cmp)` | ties under `c` broken by `key`, ordered by `cmp` |
| `c.reversed()` | the whole of `c`, reversed |

| Boxed | Primitive (`long` and `double` have the same forms) |
|---|---|
| `Predicate<Integer>` | `IntPredicate` |
| `Function<Integer, R>` / `Function<T, Integer>` | `IntFunction<R>` / `ToIntFunction<T>` |
| `UnaryOperator<Integer>` / `BinaryOperator<Integer>` | `IntUnaryOperator` / `IntBinaryOperator` |
| `Supplier<Integer>` / `Consumer<Integer>` | `IntSupplier` / `IntConsumer` |

## Challenges

1. Run `Boxing` with two lambdas instead of three. Does the JIT still remove the boxing?
2. Your service has eleven `Function<Order, Order>` steps. Would a `PricingStep` interface pay for
   its adapters?
3. Which test inputs would catch a comparator that lacks its last tie-breaker?

## Design Note: Why the JDK Has 43 Interfaces Instead of One

Java has no function types: `(int) -> int` is not a type, and generics can't range over `int`, so
each shape needs a named interface, 43 of them in `java.util.function`. In exchange, every older
one-method interface, `Runnable` and `Comparator` included, took lambdas in Java 8 without a change
to its abstract method or to existing implementations. The
price is adapters between same-shaped types, and wildcards in every signature that accepts one.

## Code Examples

- Test: [LambdaFunctionalInterfacesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaFunctionalInterfacesTest.java)
- Source: [LambdaFunctionalInterfaces.java](src/main/java/com/github/msorkhpar/claudejavatutor/lambdaexpressions/LambdaFunctionalInterfaces.java)
