`allMatch`, `anyMatch` and `noneMatch` answer a yes-or-no question about a whole stream, and each stops as soon
as the answer is known.

Write three static methods in `BatchChecks`, each a single matching call on a stream of `words`:

1. `boolean allLongerThan(List<String> words, int minLength)`: every word has more than `minLength` characters.
2. `boolean anyLongerThan(List<String> words, int minLength)`: at least one word has more than `minLength` characters.
3. `boolean noneStartsWith(List<String> words, String prefix)`: no word starts with `prefix`.

| call | answer |
|---|---|
| `allLongerThan(["apple", "banana", "cherry"], 4)` | `true` |
| `anyLongerThan(["apple", "banana", "cherry"], 6)` | `false` |
| `noneStartsWith(["apple", "banana", "cherry"], "b")` | `false` |

A batch may be empty. Decide what each answer is then from what the question means, not from a special case.
