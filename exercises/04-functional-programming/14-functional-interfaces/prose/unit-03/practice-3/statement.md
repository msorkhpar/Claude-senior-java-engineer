`Function.identity()` is the function `t -> t`: it returns its input **itself**, not a copy. Its classic use is as
the value mapper of `Collectors.toMap`, when the value should be the stream element.

Write `Index.indexBy(List<T> items, Function<T, K> key)`. It returns a map from `key.apply(item)` to `item`;
`Collectors.toMap` with `Function.identity()` is the page's way to build it.

| items | key | result |
|---|---|---|
| `["Alice", "Bob"]` | `String::toLowerCase` | `{alice=Alice, bob=Bob}` |
| `[]` | any | `{}` |

Two items may produce the same key; keep the first. The map must iterate its keys in the order they first appear in
`items`.
