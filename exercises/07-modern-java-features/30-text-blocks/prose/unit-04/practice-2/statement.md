A text block needs far fewer escapes than a traditional literal, but not none.
The page lists what is left: a backslash is still an escape character, **three
quotes in a row** would close the block, **trailing spaces** are stripped
unless fenced with `\s`, and **a quote just before the closing delimiter**
would run into it.

Write `TextBlockSource.of(String value)`. It returns Java source for a text
block whose value is `value` (which holds no `\r` and no tab):

- the opening `"""`, then `\n`;
- each line of `value` (split on `\n`), indented by eight spaces, followed by
  `\n`, except that an empty line other than the last is written with no
  indentation at all;
- the closing `"""` right after the last line, so a value ending in `\n` ends
  with an empty last line: the closing delimiter on its own line.

Inside a line, escape only what must be escaped:

- a backslash `\` is written `\\`;
- in each run of consecutive `"`, every third quote is written `\"`, so `"""`
  is written `""\"`;
- if the line ends with a space, that last space is written `\s`;
- on the last line, if the value's final character is a `"` not already
  written `\"`, it is written `\"`.

| value (as it prints) | answer (`·` is a space, `⏎` is `\n`) |
|---|---|
| `Hello`, newline, `World` | `"""⏎········Hello⏎········World"""` |
| `He said "Hello" to her.` | `"""⏎········He said "Hello" to her."""` |
| `Hello`, newline | `"""⏎········Hello⏎········"""` |
| `C:\Users` | `"""⏎········C:\\Users"""` |
| `The delimiter is """ here` | `"""⏎········The delimiter is ""\" here"""` |
| `Name··` then newline, `Bob` | `"""⏎········Name·\s⏎········Bob"""` |
| `say "hi"` | `"""⏎········say "hi\""""` |
