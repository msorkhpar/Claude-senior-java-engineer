`BiFunction<T, U, R>` takes two arguments. `Map.merge(key, value, remapping)` uses one: when `key` is already present
it calls `remapping.apply(oldValue, value)` and stores the result, otherwise it just stores `value`. With
`Integer::sum` as the `BinaryOperator`, that is a counter in one line.

Write two methods in `Tally`, using `Map.merge`:

1. `counts(List<String> words)` returns how often each word occurs, keyed by the lower-case word.
2. `combine(Map<String, Integer> a, Map<String, Integer> b)` returns a new map holding every word of either tally,
   with the counts of shared words added.

| call | result |
|---|---|
| `counts(["java", "python", "java", "java", "python"])` | `{java=3, python=2}` |
| `combine({a=1, b=2}, {b=3, c=4})` | `{a=1, b=5, c=4}` |

Input may contain capitalized, empty or whitespace-only words. The caller may keep using both maps it passed to
`combine`.
