# 10.1.2. Big O Notation

A nightly job de-duplicates order ids before billing. In staging, with 10,000 orders, the step
took about a tenth of a second. Production now sends 80,000 orders a night and the step takes over
8 seconds: eight times the data, about eighty times the time. The loop has one line in its body,
`if (!seen.contains(id))`, and `seen` is an `ArrayList`.

The table of complexity classes is easy to recite. In an interview or a code review, the harder
skill is finding the order of growth of the code in front of you. Four rules, one per heading.

## Count the Operations: Doubling n Shows the Order

Put a counter on the innermost step, run the code at n and at 2n, and divide. Three pieces of
code over a shuffled list of distinct amounts: one pass for the largest amount, a check of every
pair for a duplicate, and the JDK's own `List.sort` with a comparator that counts its calls.

```java
static int max(List<Integer> amounts) {
    int best = amounts.get(0);
    for (int a : amounts) {
        count++;
        if (a > best) best = a;
    }
    return best;
}

static boolean hasDuplicate(List<Integer> amounts) {
    for (int i = 0; i < amounts.size(); i++) {
        for (int j = i + 1; j < amounts.size(); j++) {
            count++;
            if (amounts.get(i).equals(amounts.get(j))) return true;
        }
    }
    return false;
}

static void sort(List<Integer> amounts) {
    amounts.sort((a, b) -> { count++; return Integer.compare(a, b); });
}
```

The `sorted input` column sorts the same list a second time, when it is already in order:

```text
      n          max         sort sorted input   hasDuplicate
   1000         1000         8688          999         499500
   2000         2000        19357         1999        1999000
  x2 ->        x2.00        x2.23        x2.00          x4.00
   4000         4000        42696         3999        7998000
  x2 ->        x2.00        x2.21        x2.00          x4.00
   8000         8000        93473         7999       31996000
  x2 ->        x2.00        x2.19        x2.00          x4.00
  16000        16000       202830        15999      127992000
  x2 ->        x2.00        x2.17        x2.00          x4.00
```

The ratio at each doubling gives the order. ×2 is linear. ×4 is quadratic: `hasDuplicate` makes
n(n−1)/2 comparisons. The sort's ×2.2 is n log n: doubling n doubles the n and adds one to log₂ n,
so the ratio is 2 × (1 + 1/log₂ n), which is 2.2 at n = 1,000 and falls toward 2 as n grows.

Big O keeps only that growth. The sort made 8,688 comparisons at n = 1,000, about 0.87 × n log₂ n;
the 0.87 is a constant, and Big O drops it along with lower-order terms such as the −1 in
n(n−1)/2, which matter less the larger n gets. What it keeps is enough to predict: at a million amounts `hasDuplicate` would make about
5 × 10¹¹ comparisons, `max` 10⁶.

The count also depends on the input. On a list that was already sorted, `List.sort` made n − 1
comparisons: the javadoc's implementation note says its merge sort needs "approximately n
comparisons" on nearly sorted input, and has "the performance of a traditional mergesort" on
random input. "What is the complexity of this
sort?" has two answers, and an interviewer asking it wants to hear on which input.

Reading the order from the code's shape fails when the work sits inside a call: the sort is one
line and O(n log n). The next section puts such a call inside a loop.

## A Call Inside a Loop Multiplies: List.contains in a Loop Is O(n²)

The job from the start of this page, timed with an `ArrayList` and then a `HashSet` as `seen`:

```java
static List<String> unique(List<String> ids, Collection<String> seen) {
    List<String> out = new ArrayList<>();
    for (String id : ids) {
        if (!seen.contains(id)) {          // ArrayList: a scan of everything seen so far
            seen.add(id);
            out.add(id);
        }
    }
    return out;
}
```

The two right-hand columns take a copy of the ids and call `removeAll(billed)`, where `billed` is
half of them, once as a `List` and once as a `HashSet`. Each figure is the median of three timed
runs after one untimed run, on one machine:

```text
     ids      List seen   HashSet seen   removeAll(List)   removeAll(Set)
    5000        29.0 ms         1.9 ms           24.1 ms           0.5 ms
   10000       106.3 ms         1.1 ms           87.0 ms           0.3 ms
   20000       271.8 ms         3.4 ms          262.7 ms           0.7 ms
   40000      1413.1 ms         3.5 ms         1578.0 ms           1.8 ms
   80000      8014.2 ms         4.9 ms         9607.3 ms           2.6 ms
```

`ArrayList.contains` compares the argument with each element in turn until one is equal: O(n) per
call, in a loop that runs n times, so up to n²/2 comparisons for distinct ids. That is a factor of
four per doubling in comparisons; from 20,000 up, the time on this machine grew faster still.
`HashSet.contains` computes the id's hash and compares it with the few entries in one bucket, so the work per id doesn't
grow with the set. A second run gave the same shape (81 ms and 9.3 s for the list at 10,000 and
80,000).

`removeAll` hides the same loop. `ArrayList.removeAll(c)` calls `c.contains` once for each of its
own elements, so its cost is n times the cost of `c.contains`, which is O(n) again when `c` is a
list. `retainAll` and `stream().filter(other::contains)` have the same shape. The fix is the set,
built once before the loop. Building it inside, as in `filter(id -> new HashSet<>(billed).contains(id))`,
hashes every billed id on every turn, which is quadratic again. A hash set needs `equals` and
`hashCode` that agree (units 2.4.2 and 4.4.3), and a `TreeSet` is the O(log n) choice when the
elements are only `Comparable`.

A test with a hundred orders passes in a millisecond either way, so the n² shows up only in
production.

<!-- practice 1 -->

## Amortised O(1): ArrayList.add Copies Each Element a Few Times, Not n Times

An `ArrayList` keeps its elements in an array. When the array is full, `add` allocates a larger
one and copies every element across, which is O(n) for that one call. The javadoc nonetheless
says `add` "runs in amortized constant time, that is, adding n elements requires O(n) time".

To count the copies without reflection into `ArrayList`'s private fields, we watch allocation. The
`Integer` values are created beforehand, so an `add` that allocates anything has allocated a new
array, and it copied the `size()` elements that were in the list. The allocation counter is
`com.sun.management.ThreadMXBean.getCurrentThreadAllocatedBytes()`, a HotSpot extension of the
standard bean that unit 10.2.1 uses to measure object sizes:

```java
for (Integer v : values) {
    int before = list.size();
    long b0 = THREADS.getCurrentThreadAllocatedBytes();
    list.add(v);
    long bytes = THREADS.getCurrentThreadAllocatedBytes() - b0;
    if (bytes > 0) {                                   // this add() allocated a new array
        growths++;
        copied += before;                              // ...and copied every element into it
        biggestBytes = Math.max(biggestBytes, bytes);
        biggestCopy = Math.max(biggestCopy, before);
        if (growths <= 9) sizes.append(before).append(' ');
    }
}
```

A million adds into `new ArrayList<>()` (twice, the second after warm-up, with identical
numbers), then into `new ArrayList<>(n)`:

```text
grew when size was: 0 10 15 22 33 49 73 109 163 ...
new ArrayList<>()   growths=30, copied=2,430,972 (2.43 per add), largest single add: copied 810,325, allocated 4,861,968 bytes
new ArrayList<>()   growths=30, copied=2,430,972 (2.43 per add), largest single add: copied 810,325, allocated 4,861,968 bytes
new ArrayList<>(n)  growths=0, copied=0 (0.00 per add), largest single add: copied 0, allocated 0 bytes
```

The sizes at which it grew, 10, 15, 22, 33, 49, show
the rule in JDK 21: the new capacity is the old one plus half of it. Each copy is half again as
large as the one before, so all the copies before the last add up to about twice the last one.
The total stays a small multiple of n, here 2.43 copies per add, and the average add is O(1).

Amortised O(1) does not make every add fast. One add copied 810,325 references into a 4.9 MB
array, while the 999,970 adds without growth allocated nothing. The request that hits that add
waits while every reference is copied. When n is known, `new ArrayList<>(n)` removes every copy, as the last
line shows, and `ensureCapacity(n)` does the same for a list that already exists.

The bound comes from growing in proportion to the size. A home-made growable `int[]` that counts
the elements it copies at each growth, growing by half and by a fixed 1,000 slots (warmed round):

```text
cap + cap / 2          n=  250,000  copied=      720,285 (    2.9 per add)      0 ms
cap + 1000             n=  250,000  copied=   31,127,500 (  124.5 per add)     10 ms
cap + cap / 2          n=  500,000  copied=    1,080,430 (    2.2 per add)      1 ms
cap + 1000             n=  500,000  copied=  124,755,000 (  249.5 per add)     45 ms
cap + cap / 2          n=1,000,000  copied=    2,430,972 (    2.4 per add)      4 ms
cap + 1000             n=1,000,000  copied=  499,510,000 (  499.5 per add)    248 ms
```

With a fixed step the copies per add double each time n doubles. That is O(n) per add and O(n²)
in total, and a million adds copied half a billion elements.

<!-- practice 2 -->

## At Small n, Constants and the Cache Decide

Big O compares growth, and says nothing about which code is faster at the n you actually have.
Two sorts of our own on random `int[]` arrays: insertion sort, O(n²), and a textbook top-down merge
sort, O(n log n), that copies each half into a new array:

```java
static void insertionSort(int[] a) {
    for (int i = 1; i < a.length; i++) {
        int x = a[i], j = i - 1;
        while (j >= 0 && a[j] > x) { a[j + 1] = a[j]; j--; }
        a[j + 1] = x;
    }
}

static void mergeSort(int[] a) {
    if (a.length < 2) return;
    int[] left = Arrays.copyOfRange(a, 0, a.length / 2);
    int[] right = Arrays.copyOfRange(a, a.length / 2, a.length);
    mergeSort(left);
    mergeSort(right);
    int i = 0, j = 0, k = 0;
    while (i < left.length && j < right.length) a[k++] = left[i] <= right[j] ? left[i++] : right[j++];
    while (i < left.length) a[k++] = left[i++];
    while (j < right.length) a[k++] = right[j++];
}
```

Each figure is nanoseconds per sort: the median of five runs, after two warm-up runs, each run
sorting copies of 1,000 random arrays about 20,000/n times over. Each sort is timed on its own,
so the rows for n = 8 and 16 are mostly the cost of `System.nanoTime()`, about 20 ns a pair. The last column counts the
comparisons for one of those arrays in a separate, untimed pass:

```text
     n   insertion sort       merge sort   compares (ins/merge)
     8            26 ns           121 ns            19 / 16
    16            43 ns           254 ns            68 / 44
    32           304 ns           750 ns           310 / 117
    64           699 ns          1937 ns           975 / 291
   128          1854 ns          4249 ns          4087 / 728
   256          5566 ns          9644 ns         17796 / 1716
   512         18611 ns         21127 ns         68001 / 3936
  1024         74165 ns         47414 ns        265932 / 8916
```

At 512 elements insertion sort made seventeen times the comparisons and still finished first.
Its comparisons are cheap: it shifts values inside one array and allocates nothing. Merge sort
makes two recursive calls and allocates two arrays on every call, 1,022 arrays for one sort of
512 elements. Those costs are the constants Big O drops, and up to 512 elements on this machine,
in both runs, they outweighed the growth. At 1,024 merge sort was ahead.

A membership test shows the opposite. A similar warmed loop, half of its queries absent, found a
`HashSet<Integer>` lookup faster than a scan of an `int[]` at 32 elements (2.8 ns against 5.1 ns),
the two about even at 8 to 16, and the set slightly ahead even at 4. The scan never clearly won.

> **These timings are one machine.** They come from warmed loops, two runs of each program, and
> no benchmark harness: JMH isn't in this course's local build, so the JIT's choices and the
> machine's caches are uncontrolled. Trust the shape; measure the crossover on your own hardware
> with JMH before you design around it.

For a collection of fixed small size, such as six currency codes in a `List.of(...)`, Big O
isn't the question. That list scans too: with a record whose `equals` counts its calls, `contains`
of a missing code made six calls, and it makes six however much traffic arrives.

<!-- practice 3 -->

## Summary

- **Count the operations and double n.** ×2 is linear, about ×2.2 is n log n, ×4 is quadratic;
  the count also depends on the input, as a sort of sorted data shows.
- **A call inside a loop multiplies.** `List.contains`, and `removeAll` or `retainAll` with a
  list argument, turn one loop into O(n²); a `HashSet` built once before the loop keeps it O(n).
- **`ArrayList.add` is amortised O(1) because the capacity grows by half.** The total copying is
  a small multiple of n, but one add can copy the whole list; presize when n is known. A fixed
  growth step makes the total O(n²).
- **At small n the constants decide.** Measure at the n you have; the algorithm of lower order
  wins once n is large.

## Reference

**Costs the JDK 21 javadoc states:**

| Operation | Cost stated |
|---|---|
| `ArrayList` `get`, `set`, `size`, `iterator` | constant time |
| `ArrayList.add` | amortised constant time: n adds take O(n) |
| `ArrayList` "all of the other operations" (such as `contains`, `indexOf`, `remove(Object)`) | linear time ("roughly speaking") |
| `HashMap` `get`, `put`; `HashSet` `add`, `remove`, `contains` | constant time, "assuming the hash function disperses the elements properly" |
| `TreeMap` `containsKey`, `get`, `put`, `remove` | guaranteed log(n) |
| `PriorityQueue` `offer`, `poll`, `add`, `remove()` | O(log n); `contains`, `remove(Object)` linear; `peek`, `size` constant |
| `ArrayDeque` | most operations amortised constant; `contains`, `remove(Object)` and bulk operations linear |
| `Arrays.sort(int[])` and the other primitive sorts | O(n log n) on all data sets |
| `Arrays.sort(Object[])`, `List.sort` | implementation note: a traditional mergesort's performance on random input; approximately n comparisons on nearly sorted input |
| `Collections.binarySearch` | log n on a random-access list; O(n) link traversals on a large non-random-access one |

`ArrayList.removeAll(c)` and `retainAll(c)` call `c.contains` for each element, so their cost
depends on `c`. What happens when many `HashMap` keys collide, and how `ArrayList` and
`LinkedList` compare when measured, is in unit 10.1.1. The stack depth of recursion, the space side of a recursive
algorithm, is in unit 10.2.1.

## Challenges

1. Your service's endpoint takes 40 ms at 1,000 items and 160 ms at 2,000. Before opening the code,
   what do you expect at 8,000, and which two measurements would tell you whether the growth comes
   from your code or from the database?
2. A queue consumer appends to an `ArrayList` that is cleared after every flush. Is the amortised
   cost still O(1) per add if `clear()` keeps the capacity? Count the copies with the allocation
   counter from this unit and find out.
3. You need membership tests against a set that holds 5 to 50,000 elements depending on the
   customer. One structure or two? Decide what you would measure, and on which n.

## Design Note: Why the Javadoc Promises "Amortized" and Not a Growth Factor

`ArrayList`'s javadoc says the details of the growth policy "are not specified beyond the fact that
adding an element has constant amortized time cost". JDK 21 grows by half, but code that depends on
the factor depends on something the JDK never promised. Leaving it open costs predictability. You
cannot compute from the specification which add will copy, or how much memory a list of n elements reserves. Presizing with `new ArrayList<>(n)`
or `ensureCapacity` is the specified way to get both back. The growth factor itself trades memory
for copying: growing by half leaves up to a third of the array unused after a growth, while
doubling would leave up to half unused and copy less often.

## Code Examples

- Test: [BigONotationTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/datastructures/BigONotationTest.java)
- Source: [BigONotation.java](src/main/java/com/github/msorkhpar/claudejavatutor/datastructures/BigONotation.java)
