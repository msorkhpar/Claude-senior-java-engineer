For primitive values, `java.util.function` has specializations that skip boxing each `int`
into an `Integer`: `IntPredicate` instead of `Predicate<Integer>`, `IntFunction<R>` instead of
`Function<Integer, R>`, `ToIntFunction<T>` instead of `Function<T, Integer>`, and
`IntBinaryOperator` instead of `BinaryOperator<Integer>`.

Write four static methods of `PrimitiveOps`, each built on the specialization named:

1. `int[] odds(int[] numbers)` keeps the odd numbers, in order (`IntPredicate`).
2. `List<String> padded(int[] numbers)` writes each number with at least five digits,
   zero-padded (`IntFunction<String>`).
3. `int totalLength(List<String> texts)` sums the lengths of the strings (`ToIntFunction<String>`).
4. `int product(int[] numbers)` multiplies the numbers together (`IntBinaryOperator`).

| call | answer |
|---|---|
| `odds(new int[]{1, 2, 3, 4, 5, 6})` | `[1, 3, 5]` |
| `padded(new int[]{1, 42, 999})` | `["00001", "00042", "00999"]` |
| `totalLength(List.of("a", "ab", "abc"))` | `6` |
| `product(new int[]{2, 3, 4})` | `24` |

Remember that numbers can be negative, and that an array can be empty.
