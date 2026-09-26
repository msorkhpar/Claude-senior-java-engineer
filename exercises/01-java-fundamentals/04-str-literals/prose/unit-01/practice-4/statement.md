`null` is no String at all, `""` is an empty String, and `"   "` is a blank one: not
empty, but only whitespace. `isEmpty()` and `isBlank()` tell the last two apart, and
neither can be called on `null`, which is checked first.

Write `kind(String s)` in `Kinds`. It returns:

| s | result |
|---|---|
| `null` | `"null"` |
| `""` | `"empty"` |
| `"   "`, `"\t\n"` | `"blank"` |
| `"hi"`, `" hi "` | `"text"` |

An empty String built at run time, such as `new String("")`, is `"empty"` too.
