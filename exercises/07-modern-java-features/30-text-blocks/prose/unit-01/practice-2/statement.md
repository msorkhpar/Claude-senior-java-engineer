The page calls escape sequences the *mental parsing burden* of traditional
literals: a reader has to translate `\n`, `\t`, `\"` and `\\` back into the
characters they stand for, and a doubled backslash is easy to misread.

Write `EscapeCount.count(String body)`. `body` is what sits **between the
quotes** of a traditional string literal, exactly as written in the source. It
returns how many escape sequences `body` holds. An escape sequence is a
backslash **together with the one character after it**, so `\\` is a single
escape (standing for one backslash), and the character after an escape's
backslash never starts another escape. Every `body` is well formed: no
backslash is the last character.

| body (as written in the source) | answer |
|---|---|
| `{\"name\": \"Alice\"}` | `4` |
| `Line 1\n\tIndented` | `2` |
| `C:\\Users\\admin` | `2` |
| `\\n` | `1` |
| `It\'s\r\b` | `3` |
| `plain text` | `0` |
