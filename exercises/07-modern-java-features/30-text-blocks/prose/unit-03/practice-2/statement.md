The page offers two methods that bring the text block rules to any string:
`stripIndent()` applies the compiler's incidental whitespace algorithm (where a
blank last line counts, like a closing delimiter on its own line), and
`indent(n)` adds `n` spaces to every line. Its best practices use them together
to place generated code at the right depth.

Write `Snippet.nest(String code, int level)`. `code` is a snippet pasted with
any indentation and any line endings. Return it re-based at `level`:

- the snippet's common indentation is removed, its inner indentation kept, and
  spaces at the end of each line are removed;
- a **blank** line (empty, or only spaces, the last line included) takes no
  part in the common indentation;
- every non-blank line is then indented by `4 * level` spaces, and a blank
  line stays **empty**;
- every line ends with `\n`, the last one included, and line endings are `\n`
  only.

| code (`⏎` is `\n`) | level | answer |
|---|---|---|
| `··a⏎····b` | `1` | `····a⏎······b⏎` |
| `··a⏎⏎··b` | `1` | `····a⏎⏎····b⏎` |
| `··a⏎··b` | `0` | `a⏎b⏎` |
| `····a⏎··⏎····b` | `0` | `a⏎⏎b⏎` |
| `··a\r\n··b` | `1` | `····a⏎····b⏎` |
| `··a···⏎··b` | `1` | `····a⏎····b⏎` |

(`·` is a space.) `level` is never negative, and `code` has at least one
non-blank line.
