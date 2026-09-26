`BinaryOperator<T>` combines two values of one type into a third of that type, which is exactly what
`Stream.reduce` needs. `BinaryOperator.maxBy(comparator)` builds one that keeps the larger of two values.
`IntBinaryOperator` is the primitive form, `(int, int) -> int`, used by `IntStream.reduce` with no boxing.

Write two methods in `Reductions`:

1. `longest(List<String> words)` returns the longest word; `BinaryOperator.maxBy` is the page's tool for it.
2. `product(int[] values)` returns the product of the values, reducing with an `IntBinaryOperator`.

| call | result |
|---|---|
| `longest(["hi", "hello", "hey"])` | `Optional[hello]` |
| `product([1, 2, 3, 4, 5])` | `120` |

Think about what each method should return for an empty input, and which word wins when two are equally long (the
earlier one). An empty string is a word like any other. The tests keep every product within `int` range.
