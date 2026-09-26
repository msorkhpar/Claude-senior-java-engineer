`valueOf` finds a constant by its name only. To find one by another key,
such as a code, the page builds a static unmodifiable `Map` once, so a lookup
is O(1) instead of a scan of `values()`, and returns an `Optional`. It also
warns that the key extractor must give unique keys: `toUnmodifiableMap`
throws on a duplicate.

Write the generic helper once, for any enum:

```java
static <E extends Enum<E>, K> Function<K, Optional<E>> indexBy(Class<E> type, Function<E, K> key)
```

`indexBy` builds the index from `type`'s constants **once**, reading each
constant's key a single time, and returns a lookup function that only consults
that index (it never scans the constants or calls `key` again). The lookup gives the constant whose key **equals** the one asked for,
or an empty `Optional`. If two constants have equal keys, `indexBy` itself
throws `IllegalStateException`.

The starter has `Dial` (country dialing codes: `US` 1, `UK` 44, `DE` 49,
`IE` 353, `FI` 358) and `SharedDial` (`US` 1, `CA` 1, `UK` 44).

| call | answer |
|---|---|
| `indexBy(Dial.class, Dial::code).apply(44)` | `Optional[UK]` |
| `indexBy(Dial.class, Dial::code).apply(358)` | `Optional[FI]` |
| `indexBy(Dial.class, Dial::code).apply(7)` | `Optional.empty` |
| `indexBy(SharedDial.class, SharedDial::code)` | throws `IllegalStateException` |
