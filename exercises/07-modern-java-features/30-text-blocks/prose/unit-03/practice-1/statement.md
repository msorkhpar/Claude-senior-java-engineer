The page splits a text block's leading whitespace in two. The **incidental**
whitespace is the common indentation that comes from where the block sits in
the source; the compiler removes it. Anything beyond it is **essential** and
stays in the value.

Write `Incidental.width(List<String> lines, String closing)`. `lines` are the
content lines of a text block as they appear in the source (spaces only, no
tabs). `closing` is the whitespace before the closing `"""` when the delimiter
sits on its own line, or `null` when it sits at the end of the last content
line. Return how many leading whitespace characters are incidental:

- the smallest number of leading spaces among the content lines,
- where a **blank** line (empty, or only spaces) takes no part,
- and where the closing delimiter's line, when it is on its own line, takes
  part too.

| lines | closing | answer |
|---|---|---|
| `"        root"`, `"            child"` | `null` | `8` |
| `"    Hello"`, `"    World"` | `"    "` | `4` |
| `"        a"`, `""`, `"        b"` | `null` | `8` |
| `"        a"`, `"   "`, `"        b"` | `null` | `8` |
| `"            Hello"` | `"        "` | `8` |
| `"    Hello"` | `"            "` | `4` |
