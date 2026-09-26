The page says a text block is an ordinary `String`: it is interned like any
literal, and a text block constant can be a `case` label. It also says a
literal and a text block with the same characters are even `==`, *because both
are compile-time constants*. A string built while the program runs is not.

Write `Replies.reply(String message)`. The starter declares the known messages
as text block constants. The answer is:

| message | answer |
|---|---|
| `HELLO`, the two lines `Hello` and `World` | `"greeting"` |
| `BYE`, the one line `Bye` | `"farewell"` |
| anything else | `"unknown"` |

The message may come from anywhere: a literal, a `StringBuilder`, a
`formatted()` call or `null`. It matches a known message when it has exactly
the same characters, so neither `"Hello\nWorld\n"` nor `"hello\nworld"` is `HELLO`, and a `null`
message is `"unknown"`.
