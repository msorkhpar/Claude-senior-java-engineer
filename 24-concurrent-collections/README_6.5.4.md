# 6.5.4. Collection Capacity and Resizing

The pricing service rebuilds its SKU lookup every ten minutes from 900,000 rows, and the team sized
the map for it: `new HashMap<>(900_000)`. The latency graph still shows one request every ten
minutes that waits about 25 ms longer than the rest, always during the rebuild. The map still
resizes once during the load.

## A Probe That Reads the Array Behind a Collection

A collection's capacity is the length of the array behind it, and the API doesn't show it.
`Capacity.of` reads the array by reflection, with the two `--add-opens` flags in its comment.

```java
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/** Reads the length of a collection's internal array. Run with --add-opens java.base/java.util=ALL-UNNAMED
 *  and --add-opens java.base/java.util.concurrent=ALL-UNNAMED. A diagnostic, never production code. */
public final class Capacity {

    public static int of(Object collection) {
        try {
            if (collection instanceof HashSet<?>) {
                return of(read(HashSet.class, "map", collection));
            }
            Object array = switch (collection) {
                case HashMap<?, ?> m -> read(HashMap.class, "table", m);
                case ConcurrentHashMap<?, ?> m -> read(ConcurrentHashMap.class, "table", m);
                case ArrayList<?> l -> read(ArrayList.class, "elementData", l);
                default -> throw new IllegalArgumentException(collection.getClass().getName());
            };
            return array == null ? 0 : ((Object[]) array).length;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object read(Class<?> type, String name, Object target) throws ReflectiveOperationException {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(target);
    }
}
```

The examples put the keys `1..n` into a map and print every table length it passes through:

```java
static void growth(String name, Map<Integer, Integer> map, int n) {
    StringJoiner tables = new StringJoiner(", ");
    int last = Capacity.of(map);                 // 0: no table until the first put
    for (int i = 1; i <= n; i++) {
        map.put(i, i);
        int now = Capacity.of(map);
        if (now != last) tables.add(now + " (put " + i + ")");
        last = now;
    }
    System.out.printf("%-36s %s%n", name + ":", tables);
}
```

## A HashMap Is Sized in Buckets, and It Doubles at Three-Quarters Full

Before you run it: a map created with `new HashMap<>(100)` gets 100 puts. Does its table change?

```java
growth("new HashMap<>()", new HashMap<>(), 1000);
growth("new HashMap<>(100)", new HashMap<>(100), 100);
growth("HashMap.newHashMap(100)", HashMap.newHashMap(100), 100);
```

```text
new HashMap<>():                     16 (put 1), 32 (put 13), 64 (put 25), 128 (put 49), 256 (put 97), 512 (put 193), 1024 (put 385), 2048 (put 769)
new HashMap<>(100):                  128 (put 1), 256 (put 97)
HashMap.newHashMap(100):             256 (put 1)
```

It does. `HashMap`'s constructor takes a number of **buckets** (the table's length) and rounds it
up to a power of two: 100 becomes 128. The map doubles the table as
soon as its size goes above the **threshold**, capacity × load factor, and the default load factor
is 0.75. A 128-bucket table holds 96 entries, and put 97 doubles it. The table is allocated on the
first put, not in the constructor.

The opening problem is the same arithmetic: 900,000 rounds up to 1,048,576 buckets, whose
threshold is 786,432. The rebuild resizes once, at put 786,433.

`HashMap.newHashMap(n)` (Java 19) takes the number of **entries** and picks a table that holds
them. Before Java 19 the usual request was `(int) (n / 0.75f) + 1`, the pre-Java-19 formula, which
also gives 256 for 100. `new HashSet<>(n)` and `new LinkedHashMap<>(n)` take buckets too, and so
does the three-argument `LinkedHashMap` constructor that an access-ordered cache needs. Its factory,
`LinkedHashMap.newLinkedHashMap(n)`, takes no access-order argument, so there you compute the
bucket count yourself.

## A Resize Moves Every Entry in One put

Doubling keeps the average put cheap: the moves over a whole load add up to fewer than two per
entry. The cost lands on one call. This loads the opening problem's SKUs, 900,000 distinct
`String` keys in `skus`, and times every put:

```java
static void load(String name, Map<String, Integer> map, String[] skus) {
    long slowest = 0;
    int slowestAt = 0;
    long start = System.nanoTime();
    for (int i = 0; i < skus.length; i++) {
        long t = System.nanoTime();
        map.put(skus[i], i);
        long took = System.nanoTime() - t;
        if (took > slowest) { slowest = took; slowestAt = i + 1; }
    }
    long total = System.nanoTime() - start;
    System.out.printf("%-28s total %4d ms, slowest put %5.1f ms (put %,d)%n",
            name, total / 1_000_000, slowest / 1e6, slowestAt);
}

load("new HashMap<>(900_000)", new HashMap<>(900_000), skus);
load("HashMap.newHashMap(900_000)", HashMap.newHashMap(900_000), skus);
```

Third round, after two to warm up, with a 3 GB young generation so that no collection runs during
the load:

```text
new HashMap<>(900_000)       total  115 ms, slowest put  18.9 ms (put 786,433)
HashMap.newHashMap(900_000)  total   92 ms, slowest put   1.1 ms (put 1)
```

Most of the 23 ms between the totals is one put. It took 19 ms: it allocated a table of 2,097,152
buckets and moved 786,433 entries into it while the old table was still alive. Across runs that put
took 19 to 29 ms. A resize doesn't call your `hashCode()` again (`HashMap` stores each key's hash;
1,000 puts through seven resizes made 1,000 calls); its cost is the walk over every entry.

So pre-sizing mainly removes a pause. Oversizing costs memory: `HashMap.newHashMap(1_000_000)`
allocates 2,097,152 buckets on its first put, about 8 MB with compressed references (4 bytes each,
the default below a 32 GB heap), even if only ten entries arrive.

## ConcurrentHashMap Is Sized in Entries, and Its Load Factor Only Sizes the First Table

The stock cache is shared between request threads, so it's a `ConcurrentHashMap`, and someone
copied the pre-Java-19 formula into it. The maps built for 100 get 200 puts; those built for 40 or
54 get 40:

```java
growth("new ConcurrentHashMap<>(100)", new ConcurrentHashMap<>(100), 200);
growth("new ConcurrentHashMap<>(40)", new ConcurrentHashMap<>(40), 40);
growth("new ConcurrentHashMap<>(54)", new ConcurrentHashMap<>(54), 40);
growth("new ConcurrentHashMap<>(100, 0.5f)", new ConcurrentHashMap<>(100, 0.5f), 200);
```

```text
new ConcurrentHashMap<>(100):        256 (put 1), 512 (put 192)
new ConcurrentHashMap<>(40):         64 (put 1)
new ConcurrentHashMap<>(54):         128 (put 1)
new ConcurrentHashMap<>(100, 0.5f):  256 (put 1), 512 (put 192)
```

`new ConcurrentHashMap<>(100)` is already sized for 100 entries: its javadoc says it sizes the
table "to accommodate this many elements", the meaning `newHashMap` has. Passing it the formula, 54
for 40 entries, sizes for 54 entries and gets 128 buckets where 64 hold 40. Not every count crosses
a power of two (100 and its 134 both give 256), so the waste shows on some maps and not others.

The load factor sizes only the first table: 100 entries at 0.5 asked for 200 buckets and got 256,
but the next resize still came when the size *reached* three-quarters, put 192. That is one put
earlier than `HashMap`'s rule (the default map resizes at put 12, not 13). A load factor above 0.75
makes the first table smaller, and the map still resizes at three-quarters. In
`new ConcurrentHashMap<>(n, lf, concurrencyLevel)` the last argument is a sizing hint as well: the
initial capacity is raised to at least its value.

<!-- practice 1 -->

## An ArrayList Grows by Half When Full, and Nothing Shrinks on Its Own

An `ArrayList` grows only when its array is full, to about one and a half times its length:
`new ArrayList<>()` gets 10 slots on its first add, then 15, 22, 33. Each writer thread of a
batching service reuses one list as its buffer, and one night a burst of a million records goes
through it:

```java
ArrayList<Integer> buffer = new ArrayList<>();
for (int i = 0; i < 1_000_000; i++) buffer.add(i);
System.out.println("ArrayList, 1,000,000 adds:  " + Capacity.of(buffer) + " slots");
buffer.clear();
System.out.println("after clear():              " + Capacity.of(buffer) + " slots");
buffer.trimToSize();
System.out.println("after trimToSize():         " + Capacity.of(buffer) + " slots");

HashMap<Integer, Integer> sessions = new HashMap<>();
for (int i = 0; i < 100_000; i++) sessions.put(i, i);
for (int i = 0; i < 100_000; i++) sessions.remove(i);
System.out.println("HashMap, 100,000 puts, all removed: size " + sessions.size()
        + ", " + Capacity.of(sessions) + " buckets");
```

```text
ArrayList, 1,000,000 adds:  1215487 slots
after clear():              1215487 slots
after trimToSize():         0 slots
HashMap, 100,000 puts, all removed: size 0, 262144 buckets
```

`clear()` nulls the slots so the records can be collected, and keeps the array. With compressed
references, the empty buffer holds 4.9 MB for as long as the writer keeps the list, once per writer
thread.
`trimToSize()` gives it back but leaves 0 slots, and the next batch regrows from 1: 1, 2, 3, 4, 6,
9. A buffer that should stay at its normal size after a burst is better replaced with a new list of
that size. A `HashMap` doesn't shrink either: emptied of 100,000 sessions, it keeps 262,144
buckets, and `clear()` keeps them too.

Before adding a known number of elements one by one, `ensureCapacity(minCapacity)` grows the array
once. Its argument is the total the list must hold, not the number to add: on a list that already
holds 500 elements in 549 slots, `ensureCapacity(500)` does nothing.

`CopyOnWriteArrayList` keeps no spare capacity at all: every add copies the array into a new one
exactly one longer (unit 6.5.2 shows what that costs).

<!-- practice 2 -->

<!-- practice 3 -->

## Summary

- **A `HashMap` is sized in buckets and doubles above three-quarters full.** `new HashMap<>(n)`
  resizes before n entries for many n; `HashMap.newHashMap(n)` takes the entry count.
- **A resize moves every entry in one put.** On a large map, pre-sizing removes a pause of tens of
  milliseconds, more than it speeds up the load.
- **`ConcurrentHashMap` is sized in entries, and its load factor only sizes the first table.** The
  `HashMap` formula can oversize it, and every later resize comes at three-quarters full.
- **An `ArrayList` grows by half when full, and nothing shrinks on its own.** `clear()` keeps the
  array, and an emptied `HashMap` keeps its table: trim the list, or replace either.

## Reference

**What the constructor's int means (Java 21):**

| Call | The int is | Example (ran) |
|---|---|---|
| `new HashMap<>(n)`, `new HashSet<>(n)`, `new LinkedHashMap<>(n)`, `new LinkedHashMap<>(n, lf, accessOrder)` | buckets, rounded up to a power of two | `new HashMap<>(100)`: 128, resizes at put 97 |
| `HashMap.newHashMap(n)`, `HashSet.newHashSet(n)`, `LinkedHashMap.newLinkedHashMap(n)` | entries | `newHashMap(100)`: 256, no resize |
| `new ConcurrentHashMap<>(n)`, `new ConcurrentHashMap<>(n, lf)` | entries, at load factor `lf` | 256 for 100 |
| `new ArrayList<>(n)` | slots | exactly n |

**Growth and shrinking:**

| Collection | Grows | Shrinks |
|---|---|---|
| `HashMap`, `HashSet`, `LinkedHashMap` | doubles when size > capacity × load factor (default 0.75); table allocated on first put | never; `clear()` keeps the table |
| `ConcurrentHashMap` | doubles when size reaches 0.75 × capacity (single-threaded puts), whatever load factor it was built with | never |
| `ArrayList` | when full, to old + old/2 (at least +1); `new ArrayList<>()` allocates 10 on first add | only on `trimToSize()` |
| `CopyOnWriteArrayList` | a new array, one longer, on every add | a new array, one shorter, on every remove that finds its element |
| `LinkedList` | one node per element; no capacity | |

**Edge cases (Java 21):**

| Call | Result |
|---|---|
| `new HashMap<>(-1)`, `new ArrayList<>(-1)` | `IllegalArgumentException` |
| `new HashMap<>(16, 0f)` | `IllegalArgumentException: Illegal load factor: 0.0` |
| `new HashMap<>(16, 2f)` | valid: 16 buckets hold 32 entries; it doubles at put 33 |
| `new ConcurrentHashMap<>(4, 0.75f, 64)` | 128 buckets: initial capacity raised to the concurrency level |
| `ensureCapacity(m)` with m ≤ current length | does nothing |

## Challenges

1. A map is filled once at startup and only read afterwards. Measure what a load factor of 1.0
   instead of 0.75 saves in memory and costs in `get` time, with `String` keys.
2. Your service builds one `HashMap` per request for a few dozen entries. Is pre-sizing worth a
   line of code there? Decide with a measurement.
3. A cache's size swings between 1,000 and 1,000,000 entries over a day. Design how it gives back
   memory after a peak, and what each rebuild costs the requests in flight.

## Design Note: Why HashMap's Constructor Takes a Bucket Count

`HashMap` picks a bucket with `hash & (capacity - 1)`, which needs a power-of-two table, so its
constructor asks for the thing it allocates. The number a caller actually knows is how many entries
are coming, and turning it into a bucket count means dividing by the load factor first, a step that
is easy to skip. Java 19 added `newHashMap` and a note on both constructors pointing to it, and
left the constructors' meaning alone, likely because changing it would change the memory and the resize points of
every existing `new HashMap<>(n)`. The cost is that the same int means buckets in `HashMap` and
entries in `ConcurrentHashMap`, two classes that often sit in the same service.

## Code Examples

- Test: [CollectionCapacityTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CollectionCapacityTest.java)
- Source: [CollectionCapacity.java](src/main/java/com/github/msorkhpar/claudejavatutor/concurrentcollections/CollectionCapacity.java)
