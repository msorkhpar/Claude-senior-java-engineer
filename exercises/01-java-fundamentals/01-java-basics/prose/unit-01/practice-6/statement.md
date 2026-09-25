`char` is one of the eight primitive types: an unsigned 16-bit number that
happens to print as a character. `'a' + 1` is the `int` `98`, and
`(char) ('a' + 1)` is `'b'`.

Write `CaesarShift.shift(String text, int k)`, where `0 <= k <= 25`. Every
ASCII letter moves `k` places on in the alphabet, wrapping from `z` back to `a`,
and keeps its case. Every other character is unchanged.

| text | k | answer |
|---|---|---|
| `"abc"` | `1` | `"bcd"` |
| `"xyz"` | `3` | `"abc"` |
| `"Hello, World!"` | `3` | `"Khoor, Zruog!"` |
