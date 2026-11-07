# 7.1.5. Java 21 (LTS)

A team moves its service from Java 17 to 21 in two steps. First the jar built for 17 runs on a
21 JVM for a month without a problem. Then the build's `--release` goes to 21, and a class nobody
has changed in three years stops compiling: `getFirst() in Batch cannot implement getFirst() in
List`. Java 21 only added to the library. How did that break a build?

Virtual threads (units 6.7.1 and 6.7.2) and record patterns and pattern matching for `switch`,
final in 21 (units 1.5.3 and 1.5.4), have their own units, and so does generational ZGC (unit
10.2.2), opt-in on 21 with `-XX:+ZGenerational`. This page covers sequenced collections and a few
small methods with sharp edges. Every output is from Java 21.

## Ordered Collections Now Share getFirst, getLast and reversed(); Unordered Ones Don't Get Them

Before 21, the last element of a `List` was `list.get(list.size() - 1)`, of a `Deque`
`getLast()`, of a `SortedSet` `last()`, and of a `LinkedHashSet` a loop over the whole set.
JEP 431 adds `SequencedCollection`, `SequencedSet` and `SequencedMap`, one set of first, last and
reverse methods for every collection with a defined order (listed in the Reference). `List`, `Deque`,
`SortedSet`, `SortedMap`, `LinkedHashSet` and `LinkedHashMap` now extend or implement them.
`HashSet` and `HashMap` don't: they have no order, so the type doesn't offer a first element.

`attempt` prints what a call returned, or what it threw:

```java
List<String> orders = new ArrayList<>();
SequencedMap<String, Integer> prices = new LinkedHashMap<>();
attempt("orders.getFirst()", () -> orders.getFirst());
attempt("orders.get(0)", () -> orders.get(0));
attempt("prices.firstEntry()", () -> prices.firstEntry());
attempt("TreeSet.addFirst(0)", () -> { new TreeSet<>(List.of(3, 1)).addFirst(0); return "added"; });
attempt("List.of(\"a\").addFirst(\"b\")", () -> { List.of("a").addFirst("b"); return "added"; });
```

```text
orders.getFirst()              java.util.NoSuchElementException
orders.get(0)                  java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
prices.firstEntry()            null
TreeSet.addFirst(0)            java.lang.UnsupportedOperationException
List.of("a").addFirst("b")     java.lang.UnsupportedOperationException
```

`getFirst()` on an empty collection throws, while `firstEntry()` on an empty map returns `null`,
as `NavigableMap.firstEntry()` always has. Replacing `get(0)` with `getFirst()` changes the
exception, and `NoSuchElementException` is not an `IndexOutOfBoundsException`, so a
`catch (IndexOutOfBoundsException e)` around it stops catching. A sorted set
decides positions by its comparator, so it refuses `addFirst` and `addLast`, and an unmodifiable
list refuses them as it refuses `add`.

### On LinkedHashSet and LinkedHashMap, addFirst and putLast Move an Existing Entry

```java
LinkedHashSet<String> tabs = new LinkedHashSet<>(List.of("a.txt", "b.txt", "c.txt"));
tabs.add("c.txt");
System.out.println(tabs);
tabs.addFirst("c.txt");
System.out.println(tabs);
// stock is a LinkedHashMap holding pen=3, ink=5, cap=1, in that order
stock.put("pen", 4);
System.out.println(stock);
stock.putLast("pen", 2);
System.out.println(stock);
```

```text
[a.txt, b.txt, c.txt]
[c.txt, a.txt, b.txt]
{pen=4, ink=5, cap=1}
{ink=5, cap=1, pen=2}
```

`add` and `put` keep insertion order, so an element already present stays where it was. The new
methods name a position, so they move it there. A "recently opened" list built with `add` never
moves a file the user opens again; built with `addFirst`, it moves the file to the top.

<!-- practice 1 -->

## reversed() Is a View: Writes Go Through, and Later Changes Show Through

`reversed()` copies nothing. It returns a view over the same elements, in the other order:

```java
List<String> events = new ArrayList<>(List.of("login", "view", "logout"));
List<String> newestFirst = events.reversed();
newestFirst.add("signup");                     // the caller appends to "its" list
System.out.println(events);
events.add("login");
System.out.println(newestFirst);
List<String> snapshot = List.copyOf(events.reversed());
events.add("view");
System.out.println(snapshot.size() + " in the copy, " + newestFirst.size() + " in the view");
```

```text
[signup, login, view, logout]
[login, logout, view, login, signup]
5 in the copy, 6 in the view
```

The caller's `add` went to the end of the view, which is the front of `events`. A view costs the
same at any size, which is why the JDK returns one. But a method that returns `events.reversed()`
to show a feed newest first gives every caller write access to the feed, and a caller that keeps
the view sees events that arrived later. Return
`Collections.unmodifiableList(events.reversed())` for a read-only live view, and
`List.copyOf(events.reversed())` for a snapshot. Iterating the view while the list changes
underneath throws `ConcurrentModificationException`, as iterating the list itself would.

<!-- practice 2 -->

## A Class That Already Had These Names Runs on 21 but No Longer Compiles for It

`List.getFirst()` is a new default method, so a `getFirst()` that a class already had now
overrides it, and must follow the rules for overriding. The class from the opening:

```java
import java.util.ArrayList;
import java.util.Optional;

/** A batch of rows; written for Java 17, before List had getFirst(). */
public class Batch<E> extends ArrayList<E> {
    public Optional<E> getFirst() {
        return isEmpty() ? Optional.empty() : Optional.of(get(0));
    }
}
```

`javac --release 17 Batch.java` compiles it. `javac --release 21 Batch.java` doesn't:

```text
Batch.java:6: error: getFirst() in Batch cannot implement getFirst() in List
    public Optional<E> getFirst() {
                       ^
  return type Optional<E#1> is not compatible with E#1
```

The same happens to a package-private `String getLast()` helper in a subclass of
`ArrayList<String>` (`attempting to assign weaker access privileges; was public`), and to a class
that implements both `List<E>` and `Deque<E>`, which now inherits two `reversed()` methods with
unrelated return types (`both define reversed(), but with unrelated return types`). Unit 7.1.1
covers that conflict between two defaults: the class must declare a `reversed()` whose return
type is both, as `LinkedList` does with `LinkedList<E> reversed()`.

The class file compiled for 17 still runs on 21:

```text
openjdk version "21.0.12.1" 2026-08-18 LTS
batch.getFirst(): Optional[row-1]
```

The JVM links a call by name and full descriptor, return type included, so `getFirst()` returning
`Optional` and `List.getFirst()` returning `Object` are two different methods to it. The clash is
a rule of the language, which only `javac` applies, and nothing recompiled `Batch` until the build
moved to `--release 21`. Build with `--release 21` before you deploy on 21, and rename the
colliding method (`firstRow()`). Giving it the interface's signature instead would change what
every caller that sees it as a `List` gets. A method that already matched, public with the same
return type or a subtype of it, compiles and now silently overrides the default.

<!-- practice 6 -->

## Math.clamp Narrows a long Without Wrapping and Refuses a Reversed Range

`Math.clamp(long value, int min, int max)`, new in 21, returns an `int`, so it narrows safely. A
paging endpoint reads
`?size=4294967396` as a `long`:

```java
long size = 4_294_967_396L;                    // ?size=4294967396
System.out.println(Math.clamp((int) size, 1, 500));
System.out.println(Math.clamp(size, 1, 500));
attempt("Math.toIntExact(size)", () -> Math.toIntExact(size));
attempt("Math.clamp(50, 100, 1)", () -> Math.clamp(50, 100, 1));
attempt("Math.clamp(0.0 / 0, 0.0, 1.0)", () -> Math.clamp(0.0 / 0, 0.0, 1.0));
```

```text
100
500
Math.toIntExact(size)          java.lang.ArithmeticException: integer overflow
Math.clamp(50, 100, 1)         java.lang.IllegalArgumentException: 100 > 1
Math.clamp(0.0 / 0, 0.0, 1.0)  NaN
```

The cast keeps the low 32 bits, and 4,294,967,396 is 2³² + 100, so the oversized request becomes
a page of 100 that passes every later check. Clamping the `long` gives the maximum. A reversed
range throws, where the `Math.max(min, Math.min(value, max))` that clamp replaces quietly returned
`min`. A `NaN` value comes back as `NaN`: clamp doesn't decide what an undefined ratio means, so
the caller has to.

<!-- practice 3 -->

## The Range Form of indexOf Throws Where the Old Form Returned -1

`String.indexOf(ch, beginIndex, endIndex)` (and the same with a `String` to find) searches only
`[beginIndex, endIndex)`. Unlike `indexOf(ch, fromIndex)`, it checks its range:

```java
String line = "id=42;name=Jane Doe;city=Paris";
int start = line.indexOf(';') + 1;
int end = line.indexOf(';', start);
System.out.println(start + " " + end + " " + line.indexOf('=', start, end));
System.out.println(line.indexOf('=', 40));
attempt("indexOf('=', 0, 40)", () -> line.indexOf('=', 0, 40));
int last = line.lastIndexOf(';') + 1;
int lastEnd = line.indexOf(';', last);
attempt("indexOf('=', " + last + ", " + lastEnd + ")", () -> line.indexOf('=', last, lastEnd));
```

```text
6 19 10
-1
indexOf('=', 0, 40)            java.lang.StringIndexOutOfBoundsException: Range [0, 40) out of bounds for length 30
indexOf('=', 20, -1)           java.lang.StringIndexOutOfBoundsException: Range [20, -1) out of bounds for length 30
```

The result, 10, counts from the start of the string, not from `beginIndex`. The old form returns
-1 for a `fromIndex` past the end; the range form throws for an `endIndex` past the length, a
negative `beginIndex`, or `beginIndex > endIndex`. The last one is easy to hit: the last field of
a line has no `;` after it, so `end` is -1, and a parser that works for every other field throws
on that one.

<!-- practice 4 -->

## isEmoji and StringBuilder.repeat Have Code-Point Overloads, and isEmoji Says Yes to Digits

`Character.isEmoji(int)` and its siblings (`isEmojiPresentation`, `isExtendedPictographic` and
others) take a code point. An emoji outside the Basic Multilingual Plane is two `char`s in a
`String`:

```java
String nick = "jane😀";              // "jane" and U+1F600, a grinning face
System.out.println(nick.length() + " chars, " + nick.codePointCount(0, nick.length()) + " code points");
System.out.println(nick.chars().anyMatch(Character::isEmoji));
System.out.println(nick.codePoints().anyMatch(Character::isEmoji));
System.out.println("user42".codePoints().anyMatch(Character::isEmoji));
System.out.println("user42".codePoints().anyMatch(Character::isExtendedPictographic));
System.out.println(new StringBuilder().repeat('-', 5));
System.out.println(new StringBuilder().repeat(5, '-').length());
```

```text
6 chars, 5 code points
false
true
true
false
-----
45
```

`chars()` hands `isEmoji` the two surrogate halves, and neither is an emoji. `codePoints()` finds
it, but `isEmoji` also says yes to `user42`: the methods follow the Unicode emoji data, where the
digits, `#` and `*` have the `Emoji` property because they can start a keycap sequence such as
1️⃣. A filter meant for pictographs tests `isExtendedPictographic`, which is false for digits.

`StringBuilder.repeat(int codePoint, int count)`, beside `repeat(CharSequence, int count)`, has
the same `int` parameter. A `char` widens to `int`, so `repeat(5, '-')` compiles and appends code
point 5 forty-five times.

<!-- practice 5 -->

## Summary

- **Ordered collections share `getFirst`, `getLast` and `reversed()`; `HashSet` and `HashMap`
  don't.** `getFirst()` throws on an empty collection where `firstEntry()` returns `null`; sorted
  and unmodifiable collections refuse `addFirst`. On linked sets and maps, `addFirst`/`putLast`
  move an existing entry and `add`/`put` don't.
- **`reversed()` is a view.** Writes go through to the original and later changes show; copy it
  for a snapshot, wrap it for a read-only view.
- **A class that already had these names runs on 21 but no longer compiles for it.** A different
  return type, weaker access, or `List` plus `Deque` all fail at `--release 21`.
- **`Math.clamp` narrows a `long` without wrapping and throws on `min > max`.** Clamp before you
  cast; `NaN` passes through.
- **The range form of `indexOf` throws** where the old form returned -1; its result counts from
  the start of the string.
- **`isEmoji` and `StringBuilder.repeat(int, int)` take code points**, and `isEmoji` is true for
  digits.

## Reference

**Sequenced methods (Java 21):**

| Type | First and last | `reversed()` returns | `getFirst()` when empty | Refuses |
|---|---|---|---|---|
| `List` | `getFirst`, `getLast`, `addFirst`, `addLast`, `removeFirst`, `removeLast` | `List` | throws `NoSuchElementException` | unmodifiable lists: adds, and removes from a non-empty list |
| `Deque` | the same (it had them already) | `Deque` | throws `NoSuchElementException` | |
| `LinkedHashSet` | the same; `addFirst`/`addLast` move an existing element | `SequencedSet` | throws `NoSuchElementException` | |
| `SortedSet` (`TreeSet`) | the same | `SortedSet` (`NavigableSet` for a `TreeSet`) | throws `NoSuchElementException` | `addFirst`, `addLast` |
| a class that is both `List` and `Deque` | must declare `reversed()`; an abstract one also the six first and last methods | its own type | | |
| `LinkedHashMap` | `firstEntry`, `lastEntry`, `pollFirstEntry`, `pollLastEntry` (`null` when empty); `putFirst`, `putLast` (move an existing key) | `SequencedMap` | no `getFirst()`; `sequencedKeySet().getFirst()` throws | `setValue` on an entry from `firstEntry`/`lastEntry` |
| `HashSet`, `HashMap` | none | none | | |

**Other Java 21 additions on this page:**

| Method | Watch for |
|---|---|
| `Math.clamp(long, int, int)` returns `int`; also `(long, long, long)`, `(float, ...)`, `(double, ...)` | `IllegalArgumentException` when `min > max` |
| `Math.clamp(double, double, double)` | returns `NaN` for a `NaN` value; throws for a `NaN` bound |
| `String.indexOf(ch or str, begin, end)` | throws `StringIndexOutOfBoundsException` for a range outside the string |
| `StringBuilder.repeat(int codePoint, int count)`, `repeat(CharSequence, int)` | a negative count throws `IllegalArgumentException` |
| `Character.isEmoji(int)` and siblings | code points only; true for `0`–`9`, `#`, `*` |

## Challenges

1. Your codebase has a `UserList extends ArrayList<User>` with `public User getLast()` that
   returns `null` on an empty list. It compiles on 21. What do callers that see it as a `List` get
   now, and what does `users.reversed().getFirst()` do? Run it before you guess.
2. A REST endpoint returns `items.reversed()` from a field. Measure `List.copyOf(items.reversed())`
   on 10, 10,000 and 1,000,000 elements, and decide when the view is worth the risk.
3. Find three places in your code that reach an end of a collection with `get(0)`,
   `iterator().next()` or `last()`, and decide for each whether `getFirst()` changes what happens
   when it is empty.

## Design Note: Why the New Methods Are Defaults on Old Interfaces

JEP 431 could have added a new collection hierarchy beside the old one. Then no existing
`ArrayList` would have gained `getFirst()`, and a method taking a `SequencedCollection` would
accept none of the collections already in use. Adding default methods to `List`, `Deque` and
`SortedSet` made every existing ordered collection sequenced on the day of the upgrade, without a
change to its code. The price is the one the opening paid. A subclass whose `getFirst`,
`reversed` or `addFirst` had another return type or narrower access stops compiling, and one
that matched compiles and silently overrides the new default. An interface you
publish has the same trade: a new default method is binary compatible with every implementer, and
source compatible only with those that never chose the same name with a different signature.

## Code Examples

- Implementation: [Java21Features.java](src/main/java/com/github/msorkhpar/claudejavatutor/modernjava/Java21Features.java)
- Tests: [Java21FeaturesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/modernjava/Java21FeaturesTest.java)
