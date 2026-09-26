`Stream.builder()` gives a mutable `Stream.Builder<T>`: `add` elements one at a time,
under whatever conditions you like, then call `build()` once to get the stream. After
`build()` the builder accepts nothing more.

Write `ReportLines.lines(List<String> items, boolean header, boolean footer)` using a
`Stream.Builder<String>`. The result starts with `"HEADER"` if `header` is true, then
holds the items in their order, each trimmed, and ends with `"FOOTER"` if `footer` is
true. Items that are `null` or blank are left out.

| items | header | footer | answer |
|---|---|---|---|
| `["item1", "item2"]` | `true` | `true` | `["HEADER", "item1", "item2", "FOOTER"]` |
| `["item1"]` | `false` | `true` | `["item1", "FOOTER"]` |
| `[]` | `true` | `true` | `["HEADER", "FOOTER"]` |

The items come from user input: expect stray spaces, blank entries and the odd `null`.
