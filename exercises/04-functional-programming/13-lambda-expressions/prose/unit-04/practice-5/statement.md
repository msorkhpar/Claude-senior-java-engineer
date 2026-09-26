Null checks need not break a lambda chain apart: `filter(Objects::nonNull)` drops missing
elements, and `Optional.ofNullable(...).map(...).orElse(...)` walks a path where any step may
be missing.

The file declares `record Owner(String name)` and `record Item(String id, Owner owner)`. Write
`static List<String> ownerNames(List<Item> items)` in `OwnerNames`: for each item, in order,
its owner's name, or `"Unknown"` when there is no name to give. `null` items are left out.

| items | answer |
|---|---|
| `Item("a", Owner("Ann"))`, `Item("b", Owner("Ben"))` | `["Ann", "Ben"]` |
| `Item("a", Owner("Ann"))`, `null` | `["Ann"]` |
| `Item("a", null)` | `["Unknown"]` |

"No name to give" can happen at more than one step of the path.
