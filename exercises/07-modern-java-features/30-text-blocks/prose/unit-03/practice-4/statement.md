The page's `translateEscapes()` does at run time what the compiler does in the
last step of processing a text block: it turns written escape sequences into
the characters they stand for. Its example is a value read from a file,
`Hello\nWorld\tTab` with a real backslash before the `n` and the `t`: 17
characters before, 15 after.

Write `Escapes.decode(String raw)`. `raw` holds escape sequences written out,
backslash and all. Return the string with every escape replaced by its
character: `\n`, `\t`, `\r`, `\b`, `\f`, `\s` (a space), `\"`, `\'`, `\\` (one
backslash), octal escapes such as `\101` (`A`), and a backslash right before a
line break, which removes both and joins the two lines. Each escape is read **once,
left to right**, so the `n` in `\\n` is an ordinary letter after a backslash.
A backslash followed by anything else is not an escape: throw
`IllegalArgumentException`.

| raw (as it prints) | answer (as it prints) |
|---|---|
| `Hello\nWorld\tTab` | `Hello`, newline, `World`, tab, `Tab` |
| `path=C:\\Users\\admin` | `path=C:\Users\admin` |
| `C:\\new` | `C:\new` (a backslash, then `new`) |
| `a\sb` | `a b` |
| `Octal A: \101` | `Octal A: A` |
| `Hello \`, line break, `World` | `Hello World` |
| `Invalid \x escape` | throws `IllegalArgumentException` |
