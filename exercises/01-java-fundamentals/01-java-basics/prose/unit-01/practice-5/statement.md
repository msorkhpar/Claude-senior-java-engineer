A wrapper class such as `Integer` can hold `null`, which a primitive `int` never
can. That is why wrappers are used when "no value" is a valid answer, and why
auto-unboxing a `null` throws a `NullPointerException`.

Write `BoxedSum.sum(Integer[] values)`. It returns the sum of the values that are
present, as a `long`. A `null` element is a missing value and is skipped.

| values | answer |
|---|---|
| `{1, 2, 3}` | `6` |
| `{1, null, 3, 4, null}` | `8` |
| `{Integer.MAX_VALUE, Integer.MAX_VALUE, 2}` | `4294967296` |
