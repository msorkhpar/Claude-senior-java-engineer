`Collection.iterator()` is the page's JDK example of a factory method: every
collection returns **its own** `Iterator`, and the client only sees the
interface. Each call makes **a new iterator**, which is why two loops over one
list never disturb each other.

Write `Range implements Iterable<Integer>` for the integers from `start`
(inclusive) to `end` (exclusive). `iterator()` is your factory method.

- Each `iterator()` call returns a new, independent iterator.
- `next()` after the last value throws `NoSuchElementException`.
- `new Range(a, b)` with `b < a` is refused with `IllegalArgumentException`.

| range | values |
|---|---|
| `new Range(3, 7)` | `3, 4, 5, 6` |
| `new Range(5, 5)` | none |
| two iterators over `new Range(0, 3)` | each sees `0, 1, 2` |
