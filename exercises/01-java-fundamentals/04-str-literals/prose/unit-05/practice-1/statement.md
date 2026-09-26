A `StringBuilder` changes in place: `insert(position, text)` puts text before the character
at `position` (or at the end, when `position` is the length), `delete(start, end)` removes
the characters from `start` up to but not including `end`, and `reverse()` turns the
sequence around.

Write three methods in `Edits`, each building on a `StringBuilder` of its input:

| call | result |
|---|---|
| `insertAt("Hello World", "Java ", 6)` | `"Hello Java World"` |
| `insertAt("Hello", "!", 5)` | `"Hello!"` |
| `deleteRange("Hello World", 5, 11)` | `"Hello"` |
| `deleteRange("abcdef", 1, 3)` | `"adef"` |
| `reversed("Hello")` | `"olleH"` |
