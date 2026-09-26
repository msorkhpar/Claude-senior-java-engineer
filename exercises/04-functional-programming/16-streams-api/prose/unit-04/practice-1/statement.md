`reduce` folds a stream into one value. With an identity, `reduce(identity, op)` returns a plain value, and the
identity is what an empty stream gives back, so it must leave every element unchanged (`0` for `+`, `1` for `*`).
Without one, `reduce(op)` returns an `Optional`, which is empty for an empty stream. `findFirst()` returns an
`Optional` too.

Write three static methods in `Reductions`:

1. `int product(List<Integer> numbers)`: the product of all the numbers.
2. `Optional<Integer> largest(List<Integer> numbers)`: the largest number.
3. `String firstStartingWith(List<String> words, String prefix, String fallback)`: the first word, in list order,
   that starts with `prefix`, or `fallback` when none does.

| call | answer |
|---|---|
| `product([1, 2, 3, 4])` | `24` |
| `largest([5, 3, 8, 1])` | `Optional[8]` |
| `firstStartingWith(["cherry", "banana", "blueberry"], "b", "none")` | `"banana"` |

Think about what each method owes an empty list, a list of negative numbers, and a prefix nothing starts with.
