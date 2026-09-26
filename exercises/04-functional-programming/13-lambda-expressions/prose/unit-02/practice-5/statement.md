A lambda with two or more parameters wraps them in parentheses, `(a, b) -> a + b`,
and either declares every parameter's type or none of them. A
`BinaryOperator<Integer>` is exactly such a two-parameter lambda.

Write `Zipper.zip(List<String> keys, List<Integer> values, BinaryOperator<Integer> merge)`.
It pairs `keys.get(i)` with `values.get(i)` and returns the pairs as a map. When a
key comes back a second time, its new value is combined with the one already in
the map as `merge.apply(existing, incoming)`.

| keys | values | merge | answer |
|---|---|---|---|
| `["a", "b", "c"]` | `[1, 2, 3]` | `(x, y) -> x + y` | `{a=1, b=2, c=3}` |
| `["a", "b", "a"]` | `[1, 2, 5]` | `(x, y) -> x + y` | `{a=6, b=2}` |

The two lists need not be the same length.
