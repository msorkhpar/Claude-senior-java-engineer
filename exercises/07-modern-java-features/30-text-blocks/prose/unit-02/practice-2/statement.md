A text block's value is an ordinary `String`, so every `String` method works on
it. The page's pitfall is the newline at the end: with the closing `"""` on its
own line, `"Hello\n"` is the value, and with it on the last content line the
value is `"Hello"`. Both hold **one** line.

Write `LineCount.count(String value)`. It returns how many lines `value` holds,
where each line terminator ends a line: `\n`, `\r\n` (one terminator, not two)
or a lone `\r`, as `String.lines()` counts them. A terminator at the very end ends the last line and
does not start another one. A blank line is still a line, and the empty
string holds no line at all.

| value | answer |
|---|---|
| `"Line 1\nLine 2\nLine 3"` | `3` |
| `"Hello\n"` | `1` |
| `"Hello"` | `1` |
| `"a\n\n"` | `2` |
| `"\n"` | `1` |
| `""` | `0` |
| `"a\r\nb\rc"` | `3` |
