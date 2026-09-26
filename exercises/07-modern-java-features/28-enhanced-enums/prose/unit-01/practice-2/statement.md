The page warns that `ordinal()` is a fragile thing to persist: add or reorder a
constant and every stored number points at a different constant. Persist the
`name()` instead. It also recommends a `safeValueOf` that wraps
`Enum.valueOf` in an `Optional`, because `valueOf` throws
`IllegalArgumentException` for an unknown name and `NullPointerException` for
`null`.

The starter has two releases of one enum: `StatusV1 { ACTIVE, INACTIVE }` and
the later `StatusV2 { PENDING, ACTIVE, INACTIVE }`, which added `PENDING` in
front. Write:

- `static String store(Enum<?> constant)`: the text to persist for `constant`.
- `static <E extends Enum<E>> Optional<E> load(Class<E> type, String stored)`:
  the constant of `type` that `stored` names, or an empty `Optional` when
  `stored` is `null` or names no constant. The match is exact: case and spaces count.

| call | answer |
|---|---|
| `load(StatusV1.class, store(StatusV1.INACTIVE))` | `Optional[INACTIVE]` |
| `load(StatusV2.class, store(StatusV1.ACTIVE))` | `Optional[ACTIVE]` (not `PENDING`) |
| `load(StatusV2.class, "DELETED")` | `Optional.empty` |
| `load(StatusV1.class, null)` | `Optional.empty` |

The stored text comes back from a database or a file, so it is a fresh
`String` object, never the constant's own name object.
