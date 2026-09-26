The page shows that the **closing** `"""` decides two things about a text
block's value: whether it ends with a newline, and how much indentation is
removed. Here you compute a value the way the compiler does.

Write `TextBlockValue.evaluate(String body)`. `body` is the source text that
follows the opening `"""` and its line terminator, **up to and including the
closing `"""`**. Content lines never contain a quote, a backslash, a tab, a
blank line or trailing spaces. The value is found like this:

1. Line terminators in the source (`\n`, `\r\n` or `\r`) all count as one
   line break, and the value uses `\n` between lines.
2. If only spaces come before the closing `"""` on its line, the closing
   delimiter is **on its own line**: that line is not content, and the value
   ends with a newline after the last content line. Otherwise the text before
   `"""` is the last content line and the value has no newline after it.
3. The indentation removed from every content line is the smallest number of
   leading spaces among the content lines **and the closing delimiter's line**
   when it is on its own line.

| body (`·` is a space, `⏎` a line break) | answer |
|---|---|
| `········Hello⏎········World"""` | `"Hello\nWorld"` |
| `············child⏎········root"""` | `"    child\nroot"` |
| `········Hello⏎········"""` | `"Hello\n"` |
| `············Hello⏎········"""` | `"    Hello\n"` |
| `····Hello⏎········"""` | `"Hello\n"` |
| `········"""` | `""` |
