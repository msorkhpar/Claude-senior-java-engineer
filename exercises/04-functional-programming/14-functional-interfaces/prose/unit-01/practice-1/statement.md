A consumer returns nothing: its whole job is a side effect. That makes a pair of consumers a clean way to
decouple *deciding* about an item from *what happens* to it.

Write `Router.route(List<String> items, Consumer<String> valid, Consumer<String> invalid)`. The page's shape is one
dispatcher consumer run over `items` with `forEach`. An item is valid when it has visible content, meaning it is not `null`
and `String.isBlank()` is false (so Unicode whitespace such as `"\u2003"` counts as no content). Items are handled one
at a time, in list order: every item goes to exactly one of the two handlers before the next item is looked at.

| items | `valid` receives | `invalid` receives |
|---|---|---|
| `["Alice", "", "Bob"]` | `Alice`, `Bob` | `""` |
| `["x"]` | `x` | nothing |

The page's own example list is `["Alice", "", null, "Bob", "  "]`. Make sure every one of those is routed without an
exception.
