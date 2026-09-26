The page's key point about escapes in a text block is the **order** of the
compiler's steps: line terminators are normalized to `\n` first, incidental
whitespace (and trailing whitespace) is removed second, and escape sequences
are interpreted **last**. Until that last step, `\s` or a backslash at the end
of a line is just ordinary characters.

Write `TextBlockContent.value(String raw)`. `raw` is the source between the
opening `"""`'s line terminator and the closing `"""`: its last line is the
whitespace before a closing delimiter on its own line, or the last content
line. Return the text block's value. The JDK has one method for each step:
`stripIndent()` does the first two, `translateEscapes()` the last.

| raw (`·` is a space, `⏎` a source line break) | answer |
|---|---|
| `········Tab:\there⏎········Quote: \"` | `"Tab:\there\nQuote: \""` |
| `········Hello⏎········` | `"Hello\n"` |
| `········Text···\s` | `"Text    "` (four spaces) |
| `········Hello \⏎········World` | `"Hello World"` |
| `········C:\\⏎········D:` | `"C:\\\nD:"` (a backslash, then a newline) |
| `········Line 1\r` then `\r\n` then `········Line 2` | `"Line 1\r\nLine 2"` |
