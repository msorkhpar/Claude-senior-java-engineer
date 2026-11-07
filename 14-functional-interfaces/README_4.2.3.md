# 4.2.3. Function — Transforming Values and Returning Results

A checkout service prices a basket in one statement:

```java
Map<String, Integer> prices = basket.stream()
        .collect(Collectors.toMap(Function.identity(), Checkout::priceOf));
```

In its first week in production two customers got an error page. One basket was
`[AB-1, CD-2, AB-1]`, the same SKU twice; the other was `[AB-1, ZZ-9]`, with a SKU the catalogue had
just dropped, for which `priceOf` returns `null`.

```text
java.lang.IllegalStateException: Duplicate key AB-1 (attempted merging values 1250 and 1250)
java.lang.NullPointerException
```

The failures come from what the JDK does with a function's result, or with a function of the
wrong interface type; one rule per section. How `andThen` and `compose` order their steps, and the
primitive specialisations, are in unit 4.1.3.

## UnaryOperator<T> Is a Function<T, T>, but a Function<T, T> Is Not a UnaryOperator<T>

`UnaryOperator<T>` extends `Function<T, T>` and adds no abstract method. `BinaryOperator<T>`
extends `BiFunction<T, T, T>` the same way. Some JDK methods ask for the narrower type:
`List.replaceAll` takes a `UnaryOperator<E>`, `Stream.reduce` a `BinaryOperator<T>`. Pass
`replaceAll` a function that is already in a variable:

```java
List<String> skus = new ArrayList<>(List.of(" ab-1", "cd-2 "));
Function<String, String> clean = s -> s.strip().toUpperCase();
skus.replaceAll(clean);
```

```text
ReplaceAll.java:9: error: incompatible types: Function<String,String> cannot be converted to UnaryOperator<String>
        skus.replaceAll(clean);
                        ^
```

The same lambda written inside the call compiles. A lambda has no type of its own: it takes the
type the call expects (unit 4.1.1), so `skus.replaceAll(s -> s.strip().toUpperCase())` creates a
`UnaryOperator<String>`. The variable `clean` already has a type, and `Function<String, String>` is
the supertype. Java's functional interfaces are named types, so two interfaces with the same
method shape are still two types, and a value converts only to a supertype. A
`BiFunction<Integer, Integer, Integer>` handed to `reduce(0, add)` fails the same way, and so does
`skus.replaceAll(Function.identity())`, because `identity()` returns a `Function`.

A cast silences the compiler:

```java
skus.replaceAll((UnaryOperator<String>) clean);
```

```text
java.lang.ClassCastException
```

It compiles because a `Function` variable could hold a `UnaryOperator`. It fails at run time
because the object behind `clean` was created as a `Function` and implements nothing else. Either
declare the variable as `UnaryOperator<String>`, which still goes anywhere a
`Function<String, String>` is accepted, or adapt it with a method reference, `clean::apply`, which
creates a new `UnaryOperator` that calls `clean`. For a no-op, use `UnaryOperator.identity()`.

## Collectors.toMap Throws on a Duplicate Key or a null Value Until You Say What to Do

`toMap(keyMapper, valueMapper)` won't choose between two values for one key, and it won't store a
`null` value at all. Those are the two failures from the start of the unit.

Where `HashMap.put` would silently keep the second value, the collector throws
`IllegalStateException`, as its javadoc says. A repeated key usually means the key isn't what you
thought it was: here the basket needs a quantity, and dropping one of the two lines would
undercharge.

The third argument, a `BinaryOperator<U>`, gets the value already collected and the new one, and
returns the one to keep. It doesn't help with `null`:

```java
List.of("AB-1", "ZZ-9").stream()
        .collect(Collectors.toMap(Function.identity(), Checkout::priceOf, (first, again) -> first));
```

```text
with a merge function: java.lang.NullPointerException
```

The three-argument form collects through `Map.merge`, which rejects a `null` value too. Deal with
the `null` before the collector: filter the SKU out, or, here, reject the basket with a message
that names the SKU the catalogue no longer prices.

### A Merge Function Picks a Winner, and the Last One Seen Isn't Always the Newest

A price feed sends a SKU again whenever its price changes, with a version number. The service
keeps one price per SKU. Two merge functions look equivalent:

```java
Map<String, Price> newest = feed.stream().collect(Collectors.toMap(Price::sku, Function.identity(),
        BinaryOperator.maxBy(Comparator.comparingLong(Price::version))));
Map<String, Price> lastSeen = feed.stream().collect(Collectors.toMap(Price::sku, Function.identity(),
        (older, newer) -> newer));
```

The feed held `AB-1` at version 7, then 9, then 8:

```text
maxBy(version):         Price[sku=AB-1, cents=1190, version=9]
(older, newer) -> newer: Price[sku=AB-1, cents=1300, version=8]
```

`(older, newer) -> newer` keeps whatever arrived last. That is the newest only while the feed
arrives in order, and one retried message breaks that. `BinaryOperator.minBy` and `maxBy` turn a
`Comparator` into a merge function that decides by the data.

> **Ties.** The javadoc of `minBy` and `maxBy` doesn't say which operand wins when the comparator
> returns 0. On Java 21 both return the first one, the value already in the map. If a tie can
> happen, add a tie-breaker with `thenComparing` rather than rely on that.

### Function.identity() or x -> x

`Function.identity()` and `sku -> sku` give the same map as key mappers: the checkout prints
`{CD-2=899, AB-1=1250}` with either. On Java 21, `identity()` returns the same object every time,
which its javadoc doesn't promise, and `UnaryOperator.identity()` is a different one:

```text
identity() == identity(): true
Function.identity() == UnaryOperator.identity(): false
```

Pass the one whose type the call asks for. Whether two lambdas are the same object is unit
4.1.1's subject, and no code should depend on it.

<!-- practice 1 -->

## A Remapping Function That Returns null Removes the Entry

`Map.compute`, `computeIfPresent` and `merge` take a `BiFunction` and store what it returns,
except `null`, which removes the key. A stock service reserves one unit of a SKU, and returns
`null` to mean "nothing left, do nothing":

```java
Map<String, Integer> stock = new TreeMap<>(Map.of("AB-1", 2, "CD-2", 1));
BiFunction<String, Integer, Integer> takeOne = (sku, left) -> left > 0 ? left - 1 : null;
for (int i = 0; i < 2; i++) {
    stock.computeIfPresent("CD-2", takeOne);
    System.out.println("after reserving CD-2: " + stock);
}
int left = stock.get("CD-2");
```

```text
after reserving CD-2: {AB-1=2, CD-2=0}
after reserving CD-2: {AB-1=2}
java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Map.get(Object)" is null
```

The second reservation found zero units and deleted the SKU. A stock report no longer lists it, a
bulk update such as `replaceAll` no longer reaches it, and every caller that unboxes
`stock.get(sku)` throws. To leave an entry
as it is, return the value you were given: `left > 0 ? left - 1 : left`.

This makes "update, or remove when done" one call. In a `ConcurrentHashMap` that call is atomic,
so a counter can drop its key at zero without a race between the decrement and the `remove`.
Return `null` only to remove.

When the key is absent, `merge` stores the value you pass without calling the function. `merge("ZZ-9", -1, Integer::sum)` on an empty map leaves `{ZZ-9=-1}`, a count below zero
for something that was never counted.

<!-- practice 2 -->

## A null Halfway Through an andThen Chain Fails in the Next Step; Optional.map Stops There

A shipping label takes an order's customer, the customer's address, and the address's city. An
order collected in the store has a customer with no address:

```java
Function<Order, Customer> customer = Order::customer;
Function<Customer, Address> address = Customer::address;
Function<Address, String> city = Address::city;
Function<Order, String> shipTo = customer.andThen(address).andThen(city);

Order home = new Order("o-17", new Customer("Jane Doe", new Address("Lyon")));
Order pickup = new Order("o-18", new Customer("John Roe", null));   // collects in store
System.out.println(shipTo.apply(home));
System.out.println(shipTo.apply(pickup));
```

```text
Lyon
java.lang.NullPointerException
	at java.base/java.util.function.Function.lambda$andThen$1(Function.java:88)
	at Chain.main(Chain.java:19)
```

`andThen` passes each result to the next step without looking at it. `Customer::address`
returned `null`, `Address::city` was called on it, and the exception has no message. Because the
steps are method references, no frame of theirs appears either: the trace stops at the JDK's
`andThen`. Written as lambdas, the steps would add a frame and a helpful message, but still at the
step after the one that returned `null`. Checking the order for `null` before
`apply` doesn't help, because the `null` appeared in the middle. `Optional.map` does check after
every step:

```java
Function<Order, String> shipToOrNone = order -> Optional.ofNullable(order)
        .map(Order::customer)
        .map(Customer::address)
        .map(Address::city)
        .orElse("(in store)");
```

```text
Lyon
(in store)
```

A step that returns `null` turns the `Optional` empty, and the steps after it don't run. Use this
when a missing value is a normal case, like a pickup order. When it is a bug, the empty `Optional`
hides which step was missing: fail at that step instead, with `Objects.requireNonNull(value,
message)` naming what was absent.

<!-- practice 3 -->

## A Memoized Function Must Be Pure, and Its Computation Must Not Go Back Into the Map

`key -> cache.computeIfAbsent(key, slow)` caches the function `slow` in one line; in the run,
three lookups of two keys ran `slow` twice. That only works for a pure function: a cached price
lookup keeps its first answer after the price changes. Recursion breaks it:

```java
static Map<Integer, Long> memo = new ConcurrentHashMap<>();

static long fib(int n) {
    if (n < 2) return n;
    return memo.computeIfAbsent(n, k -> fib(k - 1) + fib(k - 2));
}
```

With a new `ConcurrentHashMap` for each call:

```text
fib(13) = 233
fib(14): java.lang.IllegalStateException: Recursive update
```

`computeIfAbsent` holds the key's slot in the table while the function runs, and the function
here inserts other keys into the same map. The javadoc forbids it: "The mapping function must not
modify this map during computation", but the map throws only when it notices. Here the twelfth
entry, for 13, made the table grow while the computation for 14 still held its slot, and the resize
found it. A unit test of `fib(10)` passes. A `HashMap` notices at once and throws
`ConcurrentModificationException`. Unit 4.1.4 covers functions that change their own map in
general.

The fix is to recurse with no computation in progress: look the key up, compute the value outside
the map, and store it with `putIfAbsent`.

```java
static final Map<Integer, Long> known = new ConcurrentHashMap<>();

static long fibOutside(int n) {
    if (n < 2) return n;
    Long cached = known.get(n);
    if (cached != null) return cached;
    long value = fibOutside(n - 1) + fibOutside(n - 2);   // recurse with no computation in progress
    known.putIfAbsent(n, value);
    return value;
}
```

```text
fibOutside(90) = 2880067194370816120
```

Two threads may now compute the same key; for a pure function that costs only time.

A `null` result isn't cached. If the function returns `null`, `computeIfAbsent` records nothing,
so the function runs again on the next call:

```text
lookups for an unknown SKU asked 3 times: 3, cache {}
```

For a lookup that is slow because it goes to another service, the unknown keys are the expensive
ones. Cache the absence too, for example as an `Optional`.

<!-- practice 4 -->

## Summary

- **A `Function<T, T>` variable is not a `UnaryOperator<T>`.** A lambda takes the type the call
  asks for; a variable doesn't convert, and a cast compiles and then throws
  `ClassCastException`.
- **`Collectors.toMap` throws on a duplicate key and on a `null` value.** A merge function decides
  duplicates (`maxBy` by the data, not by arrival order); `null` values have to be dealt with before
  the collector.
- **A remapping function that returns `null` removes the entry.** Return the old value to leave it,
  and remember that `merge` stores its value for an absent key without calling the function.
- **A `null` in the middle of an `andThen` chain fails in the next step,** not in the step that
  returned it. `Optional.map` stops at the first `null`.
- **A memo built on `computeIfAbsent` needs a pure function that doesn't recurse into the map**,
  and it doesn't cache `null`.

## Reference

**JDK methods that take the narrower operator types:**

| Method | Parameter type |
|---|---|
| `List.replaceAll` | `UnaryOperator<E>` |
| `Stream.iterate(seed, f)` | `UnaryOperator<T>` |
| `Stream.reduce(identity, op)`, `Stream.reduce(op)` | `BinaryOperator<T>` |
| `Collectors.toMap(k, v, merge)` | `BinaryOperator<U>` |
| `Collectors.reducing(op)` | `BinaryOperator<T>` |

**What happens to a `null` the function returns:**

| Call | Function | When it returns `null` |
|---|---|---|
| `Map.computeIfAbsent(k, f)` | `Function` | no mapping is recorded; `f` runs again next time |
| `Map.computeIfPresent(k, f)` | `BiFunction` | the mapping is removed |
| `Map.compute(k, f)` | `BiFunction` | the mapping is removed, or stays absent |
| `Map.merge(k, v, f)` | `BiFunction`, called only if `k` has a non-null value | the mapping is removed |
| `Collectors.toMap(k, v)` | value mapper | `NullPointerException`; a duplicate key throws `IllegalStateException` |
| `Collectors.toMap(k, v, merge)` | plus a `BinaryOperator` merge | `NullPointerException` from `Map.merge` |
| `f.andThen(g)` | `Function` | `g` is called with `null` |
| `Optional.map(f)` | `Function` | the result is empty and later `map`s don't run |

## Challenges

1. A `toMap` over orders keyed by customer id throws on a duplicate in production. Is the fix a
   merge function, `groupingBy`, or a different key? What does each choice do to the totals a
   finance report reads?
2. Your memo wraps a remote lookup that sometimes returns `null` for "not found yet". How long
   should the absence stay cached, and what in your cache expresses that?
3. Measure how long `computeIfAbsent` on a `ConcurrentHashMap` holds other writers back when its
   function takes 50 ms, for writers to the same key and to other keys.

## Design Note: Why Java Has Both Function<T, T> and UnaryOperator<T>

Java 8 added function types as ordinary named interfaces, not as a new kind of type. That let
`Runnable`, `Comparator` and every single-method interface already written accept lambdas without
change, and it gave each interface a place for its own helpers: `BinaryOperator.minBy`,
`UnaryOperator.identity()`. The cost is that two interfaces with the
same shape are unrelated unless one extends the other, so a `Function<T, T>` can't go where a
`UnaryOperator<T>` is asked for, and `java.util.function` needs 43 interfaces where a language
with structural function types needs one arrow. Accept the widest type in your own
parameters (`Function<? super T, ? extends R>`, unit 4.1.3) and declare your variables as the type
the API you call asks for.

## Code Examples

- Source: [FunctionDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/FunctionDemo.java)
- Test: [FunctionDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/FunctionDemoTest.java)
