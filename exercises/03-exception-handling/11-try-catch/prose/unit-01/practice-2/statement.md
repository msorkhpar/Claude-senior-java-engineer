A broad `catch (Exception e)` masks failures that should have been seen. What the tests check is
the effect: the one failure that means "not a number" gives the fallback, and a different failure
still reaches the caller.

Write `LenientParser.parseOrDefault(String text, int fallback)`. It returns the `int` written in
`text` (surrounding whitespace ignored). When the text is not a valid `int`, it returns `fallback`.

| call | answer |
|---|---|
| `parseOrDefault("42", 0)` | `42` |
| `parseOrDefault(" -7 ", 0)` | `-7` |
| `parseOrDefault("abc", -1)` | `-1` |
| `parseOrDefault("", 9)` | `9` |

A `null` text is not bad input but a bug in the caller: it must fail with its
`NullPointerException`, not quietly turn into the fallback. And decide what "not a valid `int`"
really covers before you try to check it by hand.
