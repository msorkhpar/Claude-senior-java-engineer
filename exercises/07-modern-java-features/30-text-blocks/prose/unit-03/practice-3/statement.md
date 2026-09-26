`String.indent(n)` with a negative `n` removes indentation. The page lists its
edge case: it removes **up to** `|n|` leading whitespace characters per line,
never fewer than zero, and like every `indent` call it normalizes line endings
and ends the result with `\n`.

Write `Shift.left(String text, int n)` (`n >= 0`). It returns `text` with, on
every line, up to `n` leading whitespace characters removed:

- a line with fewer than `n` leading whitespace characters loses only those,
  never a character of its text;
- a tab is one whitespace character, like a space;
- a line may end with `\n`, `\r\n` or `\r`; the result ends every line with
  `\n` only, and **every** line ends with it, the last one included.

| text (`·` space, `→` tab, `⏎` `\n`) | n | answer |
|---|---|---|
| `····Line 1⏎····Line 2⏎` | `2` | `··Line 1⏎··Line 2⏎` |
| `··a⏎······b` | `4` | `a⏎··b⏎` |
| `→→b` | `1` | `→b⏎` |
| `hello` | `0` | `hello⏎` |
| `··a\r\n··b` | `2` | `a⏎b⏎` |
