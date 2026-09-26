A terminal operation should build its result itself rather than have a lambda `add` to a list outside the stream.
`Stream.toList()` (Java 16+) returns an unmodifiable list, and, unlike `Collectors.toUnmodifiableList()`, it
accepts `null` elements.

Write `PriceLookup.pricesOf(List<String> codes, Map<String, Integer> prices)`. It returns, for each code in list
order, its price from `prices`, or `null` in that place when `prices` has no entry for the code. Callers must not
be able to change the returned list in any way: no adding, setting or removing.

| codes | prices | answer |
|---|---|---|
| `["tea", "cake"]` | `{tea=3, cake=5}` | `[3, 5]` |
| `["cake", "cake"]` | `{tea=3, cake=5}` | `[5, 5]` |

Mind codes the price table does not know, and what a caller can do with the list you return, through any of
its methods. The answer is a snapshot: changing `codes` or `prices` afterwards does not change it.
