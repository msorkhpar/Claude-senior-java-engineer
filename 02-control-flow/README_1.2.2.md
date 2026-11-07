# 1.2.2 For Loops in Java

A nightly job has four loops that compile and read correctly in review. The 10% markdown leaves
every price unchanged. The cache warm-up refreshes shard 3 three times and shards 0 to 2 never. The
walk over ID batches never ends. The price curve sampled every 0.1 below 1.0 gets 11 points. Each
comes from what the loop variable is, or from what the condition sees at the edge of a type. Every
output below is from Java 21.

## The Enhanced for Copies Each Element Into Its Own Variable, So Assigning to It Changes Nothing

```java
int[] prices = {1999, 4500, 120};
for (int p : prices) {
    p = p * 90 / 100;                       // compiles, and changes only p
}
System.out.println("for-each: " + Arrays.toString(prices));
for (int i = 0; i < prices.length; i++) {
    prices[i] = prices[i] * 90 / 100;
}
System.out.println("indexed:  " + Arrays.toString(prices));
```

```text
for-each: [1999, 4500, 120]
indexed:  [1799, 4050, 108]
```

The Java Language Specification (14.14.2) defines the enhanced for by what it compiles to. Over an
array, it is an index loop that copies `prices[i]` into a new variable `p` at the start of each pass.
Over an `Iterable`, it calls `iterator()` once, then `hasNext()` and `next()`, and copies each
result into the variable. Either way `p` is a local. Calling a method on the copy, such as `append`
on a `StringBuilder` element, does change that object, because the copy and the element refer to the
same object. Declaring the variable `final int p` turns the assignment into a compile error.

The copy is also where unboxing happens, before the first line of the body:

```java
List<Integer> stock = Arrays.asList(12, null, 7);   // null: count unknown
int total = 0;
for (int count : stock) {
    if (count > 0) total += count;
}
```

```text
java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "java.util.Iterator.next()" is null
```

One unknown count fails the whole total, and no check in the body can prevent it. Declare the
variable as `Integer` and test it for `null`. To write back, use the index loop. To remove while
walking, use `Iterator.remove` or `removeIf` (unit 6.5.3).

<!-- practice 1 -->

## A Lambda Can Capture the Enhanced for's Variable, Not the Basic for's Counter

The warm-up builds one task per shard and hands the tasks to a pool. The first version doesn't
compile:

```java
for (int shard = 0; shard < 3; shard++) {
    jobs.add(() -> System.out.println("refresh shard " + shard));
}
```

```text
Capture.java:8: error: local variables referenced from a lambda expression must be final or effectively final
```

A lambda copies the value of a local (for an array or other object, the reference), so Java accepts
only a local that is never reassigned. A basic for has one `shard` for the whole loop, and `shard++` changes
it. A common way to silence the compiler is a holder whose reference never changes:

```java
AtomicInteger shard = new AtomicInteger();          // compiles: the reference never changes
for (; shard.get() < 3; shard.incrementAndGet()) {
    jobs.add(() -> System.out.println("refresh shard " + shard.get()));
}
jobs.forEach(Runnable::run);                        // later, as a pool would run them
```

```text
refresh shard 3
refresh shard 3
refresh shard 3
```

Each task reads the holder when it runs. Here they run after the loop, which left it at 3, a shard
that doesn't exist; on a real pool each task gets whatever value the holder has at that moment.
Copy the counter into a variable declared inside the body. The enhanced for already does that,
since its variable is declared anew for each pass; it can be captured as long as the body doesn't
assign to it:

```java
for (int shard = 0; shard < 3; shard++) {
    int s = shard;                                  // a new variable each pass
    jobs.add(() -> System.out.println("refresh shard " + s));
}
for (String region : List.of("eu", "us")) {
    jobs.add(() -> System.out.println("refresh region " + region));
}
```

```text
refresh shard 0
refresh shard 1
refresh shard 2
refresh region eu
refresh region us
```

<!-- practice 2 -->

## A Loop Ends Only When Its Condition Turns False, and int Overflow Can Keep It True

The ID walk processes batches of 100 million up to the last possible ID:

```java
int lastId = Integer.MAX_VALUE;
for (int from = 1_900_000_000; from <= lastId; from += 100_000_000) {
    System.out.println("batch from " + from);
    if (++batches == 5) break;                      // the demo stops it here
}
```

```text
batch from 1900000000
batch from 2000000000
batch from 2100000000
batch from -2094967296
batch from -1994967296
```

No `int` is greater than `Integer.MAX_VALUE`, so `from <= lastId` is never false. The update wraps
past the top into negative numbers (unit 1.1.1), walks through them, climbs back through IDs it has
already processed, and never stops. The same happens with `i <= Integer.MAX_VALUE` stepping by one.

A `long` counter can go past the last ID, so the condition turns false:

```java
for (long from = 1_900_000_000; from <= lastId; from += 100_000_000) {
    long to = Math.min(from + 100_000_000 - 1, lastId);
    System.out.println("batch " + from + ".." + to);
}
```

```text
batch 1900000000..1999999999
batch 2000000000..2099999999
batch 2100000000..2147483647
```

Because `from` is a `long`, the batch's end is too; with an `int` `from`, `from + 100_000_000 - 1`
wraps on the last batch. The bound must also be `<=`: with `from < lastId`, a last batch of a single
ID is never processed.

<!-- practice 3 -->

## A Floating-Point Counter Changes the Number of Passes, So Count With an int

How many passes does this make?

```java
for (double x = 0; x < 1.0; x += 0.1) {
    passes++;
    last = x;
}
```

```text
passes: 11, last x: 0.9999999999999999
```

`0.1` is a binary fraction slightly off its decimal value, and each addition rounds (unit 1.1.1).
After ten additions `x` is just below 1.0, so the condition holds once more. With `x <= 0.3` the
error goes the other way: the third addition gives 0.30000000000000004, and the loop makes 3 passes
instead of 4.

A `float` counter can stop moving. Between 2^24 (16,777,216) and 2^25, adjacent floats are 2 apart.
At 16,777,216, `f + 1` falls exactly between two floats, and rounding to even gives back
16,777,216:

```java
for (float f = 16_777_214f; f < 16_777_218f; f++) {
    System.out.println("f = " + f);
    if (++passes == 5) break;                       // the demo stops it here
}
```

```text
f = 1.6777214E7
f = 1.6777215E7
f = 1.6777216E7
f = 1.6777216E7
f = 1.6777216E7
```

Count passes with an `int` and compute the value from the count. Prefer `i / 10.0` to `i * 0.1`:
`10.0` is exact, so the result is the double nearest i/10, while `0.1` is already rounded and the
multiplication rounds again:

```text
i = 3: i * 0.1 = 0.30000000000000004, i / 10.0 = 0.3
```

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **The enhanced for copies each element into a new variable.** Assigning to it changes nothing,
  and unboxing a `null` element throws in the header.
- **A lambda captures values, so it needs a variable that never changes.** Copy the basic for's
  counter inside the body; a shared holder gives every task the final value.
- **A loop ends only when its condition is false.** An `int` bound at `MAX_VALUE`, or a step that
  overflows, keeps it true: count in `long`.
- **A floating-point counter makes the wrong number of passes, and a `float` can stop moving.** Count
  with an `int` and compute the value.

## Reference

| Expression after `:` | Compiles? | What runs |
|---|---|---|
| an array | yes | the array expression is evaluated once; index loop, `x = a[i]` each pass |
| an `Iterable` | yes | `iterator()` once, then `hasNext()`/`next()`; `x = next()` each pass |
| a `Stream` | no | `(Iterable<T>) stream::iterator` compiles, and can be walked only once |
| `null` | yes | `NullPointerException` before the first pass |

## Challenges

1. A loop must visit every `int` from 0 up to and including `MAX_VALUE` exactly once. Write it
   without a `long`.
2. Your tasks capture a loop variable and run on a pool. Which test catches a shared holder, and
   would it fail every time?
3. A report samples a curve from 0 to 2.5 every 0.05. Predict the passes of a `double` counter
   with `<` and with `<=`, then run it.

## Design Note: Why the Enhanced for Hides the Index

The enhanced for, added in Java 5, gives the body neither the index nor the iterator, so the same
loop reads alike over an array and over any collection. The cost: the body can't write back, skip
ahead or remove, so a loop that needs one of these is rewritten as an index or iterator loop. And
code that assigns to the variable still compiles, unless the variable is declared `final`.

## Code Examples

- Test: [ForLoopTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/controlflow/ForLoopTest.java)
- Source: [ForLoop.java](src/main/java/com/github/msorkhpar/claudejavatutor/controlflow/ForLoop.java)
