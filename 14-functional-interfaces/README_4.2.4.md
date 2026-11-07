# 4.2.4. Predicate — Testing Values and Returning Boolean Results

The seat-reservation service releases expired holds every night. Its for-each loop threw
`ConcurrentModificationException` in staging, so it became `holds.removeIf(Hold::expired)`. A week
later the job dies in production with `UnsupportedOperationException`, on a night when no hold had
expired. The predicate was right both times.

`Predicate<T>` is one method, `boolean test(T t)`. Composing predicates with `and`, `or` and
`Predicate.not` is unit 4.1.3; here the subject is the methods that take one. The examples use
these records:

```java
record Hold(String seat, int minutesLeft) {
    boolean expired() { return minutesLeft <= 0; }
    @Override public String toString() { return seat; }
}

enum Status { NEW, PAID, SHIPPED, CANCELLED }
record Order(int id, String customer, Status status, int total) {
    @Override public String toString() { return "#" + id; }
}
```

Every output is from Java 21.

## removeIf Is the Safe Way to Remove While Iterating, and an Immutable List Refuses It Even When Nothing Matches

Four holds, of which B1 and B2 have expired. Pause before the output: in the first list the
expired holds are second and fourth, in the second they are last. What does each loop leave?

```java
for (Hold h : holds) {
    if (h.expired()) holds.remove(h);
}
```

```text
for-each: java.util.ConcurrentModificationException
for-each: [A1, A2, B2]
```

The for-each runs on the list's `Iterator`. `remove` changes the list behind it, and the
iterator's next call to `next()` sees the change count move and throws. In the second list,
removing B1 shrinks the list to three, and `hasNext()` only compares its position, 3, with the
size, 3. The loop ends without calling `next()`, so nothing throws and B2 is never tested. That
seat stays held for another night.

`removeIf` hands the loop to the collection:

```java
boolean changed = holds.removeIf(Hold::expired);
```

```text
removeIf: [A1, A2], changed=true
```

Java 21's `ArrayList` tests every element first and then moves the survivors down once, so no
element is skipped. The result says whether anything was removed.

Whether the call is allowed at all is up to the list. The `List.of` javadoc says every mutator
throws, and the `removeIf` of `List.of`, `List.copyOf` and `Stream.toList()` throws before testing
a single element. A fixed-size `Arrays.asList`
uses the default `removeIf`, which removes through the iterator, so it throws only when an element
matches:

```text
List.of, nothing expired: java.lang.UnsupportedOperationException
Arrays.asList, nothing expired: changed=false
Arrays.asList, one expired: java.lang.UnsupportedOperationException: remove
```

The release job in the opening had been handed an unmodifiable copy of the holds. Had it been
given an `Arrays.asList`, it would fail only on nights with an expired hold, and tests without one
stay green.
When the caller's list may be immutable, filter into a new list and return that.

<!-- practice 1 -->

## filter Keeps Every Match; takeWhile Stops at the First false

Log lines carry the minute they were written, and two servers write them. A report wants the lines
before minute 10, once from the lines sorted by minute and once from the second server's lines
appended to the first's:

```java
Predicate<Integer> beforeTen = minute -> minute < 10;
for (List<Integer> minutes : List.of(sorted, appended)) {
    System.out.println(minutes);
    System.out.println("  filter:    " + minutes.stream().filter(beforeTen).toList());
    System.out.println("  takeWhile: " + minutes.stream().takeWhile(beforeTen).toList());
    System.out.println("  dropWhile: " + minutes.stream().dropWhile(beforeTen).toList());
}
```

```text
[1, 2, 4, 6, 9, 11, 15]
  filter:    [1, 2, 4, 6, 9]
  takeWhile: [1, 2, 4, 6, 9]
  dropWhile: [11, 15]
[1, 4, 9, 15, 2, 6, 11]
  filter:    [1, 4, 9, 2, 6]
  takeWhile: [1, 4, 9]
  dropWhile: [15, 2, 6, 11]
```

On the sorted list both give the same lines, and `takeWhile` tested 6 elements where `filter`
tested 7. `takeWhile` returns the longest prefix that passes: at the first `false` a sequential
stream stops and never tests the rest. `dropWhile` returns the rest, starting at that first failing
element. So on the appended lines the report lost minutes 2 and 6, with no error.

`takeWhile` is right when the order is part of the meaning: a sorted input you want to stop
reading early, or a leading run such as a file's header. Anywhere else, use `filter`.

> **Unordered streams differ.** On a stream with no encounter order, the javadoc lets `takeWhile`
> return any subset of the matching elements.

<!-- practice 2 -->

## asPredicate Finds the Pattern Anywhere; asMatchPredicate Needs the Whole String

A voucher code is lower-case letters followed by digits. The validator was built from the
pattern:

```java
Pattern voucher = Pattern.compile("[a-z]+\\d+");
Predicate<String> found = voucher.asPredicate();
Predicate<String> matched = voucher.asMatchPredicate();
```

```text
abc123         asPredicate=true  asMatchPredicate=true
abc123x        asPredicate=true  asMatchPredicate=false
<b>abc123</b>  asPredicate=true  asMatchPredicate=false
```

`asPredicate()` tests `matcher(s).find()`: true if the pattern occurs anywhere in the input.
`asMatchPredicate()`, since Java 11, tests `matcher(s).matches()`: the whole input must match.

Anchoring the pattern with `^` and `$` and keeping `asPredicate` comes close, but isn't the same:

```text
abc123     ^...$ asPredicate=true  asMatchPredicate=true
#abc123    ^...$ asPredicate=false asMatchPredicate=false
abc123x    ^...$ asPredicate=false asMatchPredicate=false
abc123\n   ^...$ asPredicate=true  asMatchPredicate=false
```

`^` and `$` tie the match to the ends of the input, but `$` also matches just before one final line
terminator, so a code with a trailing newline passes and reaches the database with it. For validation, use `asMatchPredicate`.

## Predicate.isEqual Is Null-Safe on Both Sides; equals Called Through a Lambda Is Not

A report counts the orders in one currency from their currency codes, where `null` means "no
currency recorded". Three ways to write it, run against `Arrays.asList("EUR", null, "USD", "EUR")`:

```java
currencies.stream().filter(Predicate.isEqual(wanted)).count();
currencies.stream().filter(c -> c.equals(wanted)).count();
currencies.stream().filter(wanted::equals).count();
```

```text
wanted EUR:
  isEqual(wanted):       2
  c -> c.equals(wanted): NullPointerException
  wanted::equals:        2
wanted null:
  isEqual(wanted):       1
  c -> c.equals(wanted): NullPointerException
  wanted::equals:        NullPointerException before any element
```

`Predicate.isEqual(target)` tests `Objects.equals(target, t)`, so a `null` on either side is just a
value, and `isEqual(null)` matches only `null`. The lambda fails at the first `null` element. The
method reference fails earlier: `wanted::equals` evaluates `wanted` when the predicate is created,
and a `null` receiver throws on that line, before any element is tested.

`isEqual` still compares with `equals`, so the types must match: `Predicate.isEqual(1).test(1L)` is
`false`, because an `Integer` never equals a `Long`.

<!-- practice 3 -->

## Optional Criteria Reduce from the Operator's Identity: x -> true for and, x -> false for or

A search form has optional fields. The service turns each filled-in field into a
`Predicate<Order>` and combines the list:

```java
static Predicate<Order> all(List<Predicate<Order>> criteria, Predicate<Order> identity) {
    return criteria.stream().reduce(identity, Predicate::and);
}

static Predicate<Order> any(List<Predicate<Order>> options, Predicate<Order> identity) {
    return options.stream().reduce(identity, Predicate::or);
}
```

Pause: the user fills in nothing. Which identity lists every order?

```text
and, no criteria:   identity true [#1, #2, #3, #4, #5]  identity false []
and, acme, >= 50:   identity true [#1]  identity false []
or, PAID|SHIPPED:   identity false [#2, #3, #4]  identity true [#1, #2, #3, #4, #5]
or, no statuses:    identity false []
```

The starting value is what an empty list means, and it is combined with every element, so it must
not change the answer. `x && true` is `x`, so `and` starts from `x -> true`. `x || false` is `x`,
so `or` starts from `x -> false`. With the other value, `and` rejects every order whatever the
user typed, and `or` accepts every order.

The version of this bug that ships is `reduce(Predicate::and).orElse(x -> false)`: right for every
form with a field filled in, and empty for the form with none.

The `no statuses` line follows the algebra, and the form needs something else. "Any of these
statuses" with no box ticked means "don't filter on status". The or-reduction of nothing is
`x -> false`, and ANDed with the other criteria it empties the result. Add a criterion only when
its field holds something.

`allMatch` on an empty stream returns `true` for the same reason, so an empty code passes a
digits-only check:

```text
"".chars().allMatch(Character::isDigit): true
```

<!-- practice 4 -->

## A Predicate That Remembers Answers Differently in Parallel and on Its Second Use

`Stream` has no "distinct by key", and a common answer is a predicate that remembers what it has
seen. `Set.add` returns `false` for a key already present:

```java
static <T, K> Predicate<T> distinctBy(Function<? super T, K> key) {
    Set<K> seen = new HashSet<>();
    return t -> seen.add(key.apply(t));
}

static final Predicate<Order> FIRST_PER_CUSTOMER = distinctBy(Order::customer);
```

Kept in a field so it is built only once, it serves two reports:

```text
first call:  [#1, #3, #4]
second call: []
```

The set lives as long as the predicate, so the second report found every customer already seen.

On a parallel stream the `HashSet` is updated from several threads, with the losses unit 4.2.1
shows for `ArrayList`. A thread-safe set (`ConcurrentHashMap.newKeySet()`) stops the lost updates,
and still keeps the wrong orders. Here are 200,000 orders over 1,000 customers, where customer
`c7`'s first order is `#7` (20 cores; the counts vary by run):

```text
run 1: HashSet kept 1000, concurrent set kept 1000 (1000 not the customer's first; c7 kept #133007)
run 2: HashSet kept 1008, concurrent set kept 1000 (1000 not the customer's first; c7 kept #172007)
run 3: HashSet kept 1821, concurrent set kept 1000 (1000 not the customer's first; c7 kept #33007)
```

The concurrent set keeps one order per customer, but the thread that reached a key first wins,
and in these runs that was never the first order. `filter` keeps the passing elements in
encounter order; it promises nothing about the order in which it tests them.

When "first" matters, let a collector decide. A non-concurrent collector combines partial results
in encounter order, so a merge function that keeps the earlier value keeps the first, in parallel
too:

```java
Map<String, Order> first = big.parallelStream()
        .collect(Collectors.toMap(Order::customer, o -> o, (earlier, later) -> earlier, LinkedHashMap::new));
```

```text
toMap run 1: kept 1000, c7 kept #7
```

A stateful predicate is safe only when it is created for one sequential pipeline and dropped with
it.

<!-- practice 5 -->

## BiPredicate and IntPredicate Compose Like Predicate, Without not or isEqual

`BiPredicate<T, U>` tests two values. `Files.find` takes one, so the test can read a file's
attributes without a second call:

```java
BiPredicate<Path, BasicFileAttributes> javaSource =
        (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java");
try (Stream<Path> found = Files.find(Path.of("."), 1, javaSource)) {
    System.out.println("sources: " + found.map(Path::getFileName).sorted().toList());
}
```

`IntPredicate` (like `LongPredicate` and `DoublePredicate`) tests a primitive, and is what
`IntStream.filter` takes. Both have `and`, `or` and `negate`, and neither has the static `not` or
`isEqual`: `Predicate.not(digit)` on an `IntPredicate` doesn't compile. Use `negate()`:

```java
IntPredicate digit = Character::isDigit;
System.out.println("SKU-0042 non-digits: " + "SKU-0042".chars().filter(digit.negate()).count());
```

```text
SKU-0042 non-digits: 4
```

<!-- practice 6 -->

## Summary

- **`removeIf` removes safely while iterating.** A for-each removal throws or silently skips the
  last element, and `List.of` refuses `removeIf` even when nothing matches.
- **`takeWhile` stops at the first `false`.** On unsorted input it drops matches that `filter`
  keeps.
- **`asPredicate` finds; `asMatchPredicate` matches the whole string.** `^...$` still lets a
  trailing newline through.
- **`Predicate.isEqual` is null-safe on both sides**; `c.equals(w)` and `w::equals` throw.
- **Reduce optional criteria from `x -> true` for `and` and `x -> false` for `or`**, and leave out
  a criterion whose field is empty.
- **A predicate with state** answers differently on reuse and arbitrarily in parallel.
- **`BiPredicate` and `IntPredicate`** have `and`, `or` and `negate`, but no `not` or `isEqual`.

## Reference

**Where the JDK takes a predicate:**

| Call | What the predicate decides |
|---|---|
| `Collection.removeIf(p)` | which elements are removed; returns whether any were |
| `Stream.filter(p)` | which elements pass; every element the pipeline pulls is tested |
| `Stream.takeWhile(p)` / `dropWhile(p)` | where the leading run ends; sequentially, `takeWhile` tests nothing after the first `false` |
| `Stream.anyMatch` / `allMatch` / `noneMatch` | the answer; on an empty stream `false` / `true` / `true` |
| `Optional.filter(p)` | whether the value is kept |
| `Files.find(start, depth, bp)` | which paths are returned (a `BiPredicate<Path, BasicFileAttributes>`) |

**`removeIf` on (ran on Java 21):**

| List | Nothing matches | Something matches |
|---|---|---|
| `ArrayList`, `LinkedList` | `false` | removes, `true` |
| `List.of`, `List.copyOf`, `Stream.toList()`, `Collections.unmodifiableList` | `UnsupportedOperationException` | `UnsupportedOperationException` |
| `Arrays.asList` | `false` | `UnsupportedOperationException` |

When the predicate threw part-way, the `ArrayList` in our run was unchanged and the `LinkedList`
had already lost the matching elements tested before the throw. The javadoc promises neither.

**Factories:**

| Factory | Tests |
|---|---|
| `Pattern.asPredicate()` | `matcher(s).find()` |
| `Pattern.asMatchPredicate()` (Java 11) | `matcher(s).matches()` |
| `Predicate.isEqual(x)` | `Objects.equals(x, t)` |
| `Predicate.not(p)` (Java 11) | `!p.test(t)` |

## Challenges

1. A cleanup must remove expired entries from a list that some callers create with `List.of`.
   Do you return a new list, copy defensively, or document the requirement? What does each cost
   the callers?
2. A search form has ten optional fields, and product asks for "match any field" as a second mode.
   Design one builder for both modes, and say what an empty form returns in each.
3. Write `distinctBy` so it is safe to share, or explain why it can't be, and measure what the
   `toMap` alternative costs on 10 million elements.

## Design Note: Handing the Loop to the Collection

With `removeIf` the collection runs the loop instead of the caller. That lets `ArrayList` test
everything first and compact once, so no element can be skipped. It also means the caller no
longer controls the loop: the collection decides when the predicate runs, how often, and whether
it runs at all, as `List.of` does when it throws before calling it. Every method in the first
reference table works this way, so a predicate passed to one should answer from its argument
alone.

## Code Examples

- Source: [PredicateDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/PredicateDemo.java)
- Test: [PredicateDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/functionalinterfaces/PredicateDemoTest.java)
