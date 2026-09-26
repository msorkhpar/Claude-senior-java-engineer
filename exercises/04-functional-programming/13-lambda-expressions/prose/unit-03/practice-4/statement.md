For primitive values, `java.util.function` has specializations that skip boxing each `int`
into an `Integer`: `IntPredicate` instead of `Predicate<Integer>`, `IntFunction<R>` instead of
`Function<Integer, R>`, `ToIntFunction<T>` instead of `Function<T, Integer>`, and
`IntBinaryOperator` instead of `BinaryOperator<Integer>`.

Write four static methods of `PrimitiveOps`. The specialization in brackets is the natural fit for each; the tests
check the results, not which interface you chose:

1. `int[] odds(int[] numbers)` keeps the odd numbers, in order, repeats included (`IntPredicate`).
2. `List<String> padded(int[] numbers)` writes each number exactly as `String.format("%05d", n)`
   does: at least five characters, zero-padded after any sign, never cut (`-42` → `"-0042"`,
   `123456` → `"123456"`) (`IntFunction<String>`).
3. `int totalLength(List<String> texts)` sums `String.length()` of the strings, counted in chars,
   not bytes (`ToIntFunction<String>`).
4. `int product(int[] numbers)` multiplies the numbers together (`IntBinaryOperator`).

| call | answer |
|---|---|
| `odds(new int[]{1, 2, 3, 4, 5, 6})` | `[1, 3, 5]` |
| `padded(new int[]{1, 42, 999})` | `["00001", "00042", "00999"]` |
| `totalLength(List.of("a", "ab", "abc"))` | `6` |
| `product(new int[]{2, 3, 4})` | `24` |

Remember that numbers can be negative, and that an array can be empty.
