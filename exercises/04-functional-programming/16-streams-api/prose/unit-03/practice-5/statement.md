`mapToInt`, `mapToLong` and `mapToDouble` move from a `Stream<T>` to a primitive
stream, which brings `sum()` and `average()` with it and avoids boxing every number.
`IntStream.average()` returns an `OptionalDouble`, because an empty stream has no
average.

Write three methods in `Lengths`:

1. `totalLength(List<String> words)` returns the sum of the words' lengths.
2. `average(List<String> numbers)` parses each string as an `int` (`Integer.parseInt`,
   so `"2.5"` throws `NumberFormatException`) and returns the exact average as an
   `OptionalDouble`, even for values near `Integer.MAX_VALUE`.
3. `sum(List<Integer> values)` returns the sum of the values as a `long`.

| call | answer |
|---|---|
| `totalLength(["hello", "world", "!"])` | `11` |
| `average(["2", "4", "6"])` | `OptionalDouble[4.0]` |
| `sum([1, 2, 3])` | `6` |

Choose the primitive stream that can hold the answer, and let "no numbers" stay
visible to the caller.
