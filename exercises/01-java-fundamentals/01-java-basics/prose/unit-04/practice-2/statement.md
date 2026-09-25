`int` arithmetic overflows silently: `65536 * 65536` is `0`. For checked
arithmetic, the page points at `Math.addExact`, `Math.multiplyExact` and friends,
which throw an `ArithmeticException` instead of wrapping.

Write `CheckedArithmetic.multiplyAdd(int a, int b, int c)`. It returns
`a * b + c`, or throws an `ArithmeticException` when **either step** overflows
the `int` range. A result exactly at a limit is fine.

| a, b, c | answer |
|---|---|
| `2, 3, 4` | `10` |
| `65536, 65536, 0` | `ArithmeticException` |
| `Integer.MAX_VALUE, 1, 1` | `ArithmeticException` |
| `Integer.MAX_VALUE, 1, 0` | `2147483647` |
