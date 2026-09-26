A lambda inside `filter` or `map` should compute, not change things outside itself. A counter
bumped inside `filter` counts what the filter *looked at*, and a counter bumped inside `peek`
may never run at all: since Java 9, `list.stream().peek(...).count()` can answer from the
list's size without running `peek`. Collect the result, then measure it.

The file declares `record Entry(String id, boolean valid)` and
`record Report(List<String> validIds, long validCount, long seen)`. Write
`static Report audit(List<Entry> entries)` in `Audit`:

- `validIds`: the ids of the valid entries, in order;
- `validCount`: how many entries are valid;
- `seen`: how many entries there are in all.

| entries | report |
|---|---|
| `a` valid, `b` invalid, `c` valid | `Report([a, c], 2, 3)` |
| none | `Report([], 0, 0)` |

Each number must be right on its own; no lambda in your pipeline needs to touch a variable outside it.
