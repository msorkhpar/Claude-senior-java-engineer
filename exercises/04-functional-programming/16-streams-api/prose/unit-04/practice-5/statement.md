`Collectors.teeing(first, second, merger)` (Java 12+) feeds every element to two collectors at once and merges
their two results, so one pass over the data yields two views of it. Primitive streams bring their own
reductions: `IntStream.average()` returns an `OptionalDouble`.

`OnePass.MinMax` is a record `(int min, int max)`. Write two static methods in `OnePass`:

1. `Optional<MinMax> range(List<Integer> numbers)`: the smallest and the largest number (`teeing` can find both in one pass).
2. `double mean(List<Integer> numbers)`: the average of the numbers, or `0.0` when there are none.

| call | answer |
|---|---|
| `range([3, 1, 4, 1, 5, 9, 2, 6, 5, 3])` | `Optional[MinMax[min=1, max=9]]` |
| `mean([3, 1, 4, 1, 5, 9, 2, 6, 5, 3])` | `3.9` |

Both methods may be handed an empty list, or a list with no positive number in it.
