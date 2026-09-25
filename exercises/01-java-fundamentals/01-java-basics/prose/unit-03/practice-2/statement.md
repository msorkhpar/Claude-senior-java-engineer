A narrowing cast such as `(int) someLong` keeps only the low 32 bits:
`(int) 2147483648L` is `-2147483648`. The page's advice is to **check the range
before casting**.

Write `Narrowing.clampToInt(long value)`. A value that fits in an `int` is
returned unchanged. A value above `Integer.MAX_VALUE` returns `Integer.MAX_VALUE`,
and a value below `Integer.MIN_VALUE` returns `Integer.MIN_VALUE`.

| value | answer |
|---|---|
| `42L` | `42` |
| `2147483648L` | `2147483647` |
| `-2147483649L` | `-2147483648` |
