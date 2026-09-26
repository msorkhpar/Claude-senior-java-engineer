A lambda cannot write to a local variable of its method, so
`items.forEach(item -> count++)` does not compile. The tempting way around it
is to keep the count somewhere the lambda *can* write. The page's better advice
is to avoid the side effect and let a stream do the counting.

Write `MatchCounter.countMatches(List<String> items, Predicate<String> condition)`,
an instance method that returns how many items the condition accepts. The list
may contain `null` items: they are never counted, and the condition is never
called with `null`.

| items | condition | answer |
|---|---|---|
| `["apple", "banana", "apricot", "cherry"]` | `s -> s.startsWith("a")` | `2` |
| `[]` | any | `0` |
| `["apple", null, "avocado"]` | `s -> s.startsWith("a")` | `2` |

The same `MatchCounter` may be asked many times; each answer must stand on its own.
