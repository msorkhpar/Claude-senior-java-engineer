A consumer returns nothing: its whole job is a side effect. That makes a pair of consumers a clean way to
decouple *deciding* about an item from *what happens* to it.

Write `Router.route(List<String> items, Consumer<String> valid, Consumer<String> invalid)`. The page's shape is one
dispatcher consumer run over `items` with `forEach`. An item is valid when it has visible content; every item goes to
exactly one of the two handlers, in list order.

| items | `valid` receives | `invalid` receives |
|---|---|---|
| `["Alice", "", "Bob"]` | `Alice`, `Bob` | `""` |
| `["x"]` | `x` | nothing |

The page's own example list is `["Alice", "", null, "Bob", "  "]`. Make sure every one of those is routed without an
exception.
