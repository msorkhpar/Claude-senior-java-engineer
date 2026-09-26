Before text blocks, the only string literal Java had was the traditional
double-quoted one. The page lists what that costs: every `"` inside it is
written `\"`, every `\` is written `\\`, and every newline is written `\n`,
because the literal cannot span source lines.

Write `JavaLiteral.quote(String value)`. It returns the **source text** of the
traditional literal whose value is `value`: the value between two double
quotes, with these characters written as escapes:

| character in the value | written as |
|---|---|
| `"` | `\"` |
| `\` | `\\` |
| newline | `\n` |
| carriage return | `\r` |
| tab | `\t` |

Every other character, the single quote `'` included, is written as it is.

| value (as it prints) | answer (as it prints) |
|---|---|
| `He said "Hi"` | `"He said \"Hi\""` |
| `C:\Users\admin` | `"C:\\Users\\admin"` |
| the regex `\d+` | `"\\d+"` |
| `Line 1`, newline, tab, `Indented` | `"Line 1\n\tIndented"` |
| `It's` | `"It's"` |
