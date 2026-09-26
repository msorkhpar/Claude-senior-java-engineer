A broad `catch (Exception e)` masks failures that should have been seen. What the tests check is
the effect: the one failure that means "not a number" gives the fallback, and a different failure
still reaches the caller.

Write `LenientParser.parseOrDefault(String text, int fallback)`. It removes surrounding whitespace as
`String.strip()` does (Unicode white space), and returns the `int` that `Integer.parseInt` reads from
what is left: decimal only, so `"010"` is ten and `"0x10"` is not a number. When `Integer.parseInt`
rejects the text, it returns `fallback`.

| call | answer |
|---|---|
| `parseOrDefault("42", 0)` | `42` |
| `parseOrDefault(" -7 ", 0)` | `-7` |
| `parseOrDefault("abc", -1)` | `-1` |
| `parseOrDefault("", 9)` | `9` |

A `null` text is not bad input but a bug in the caller: it must fail with its
`NullPointerException`, not quietly turn into the fallback. And decide what "not a valid `int`"
really covers before you try to check it by hand.
