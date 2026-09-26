A stream happily carries `null` elements, but a method call on one, or `sorted()`
with natural ordering, throws `NullPointerException`. Filter the nulls out
explicitly, before anything touches them.

Write `SortedNames.sortedUpper(List<String> names)`. It returns the names upper-cased
with `Locale.ROOT` and sorted in the natural `String` order of the upper-cased text
(character by character, so `"AB"` comes before `"A_"`), leaving out `null`
entries. A name that repeats is kept each time.

| names | answer |
|---|---|
| `["banana", "cherry", "apple"]` | `["APPLE", "BANANA", "CHERRY"]` |
| `["banana", null, "apple", null]` | `["APPLE", "BANANA"]` |
| `[null, null]` | `[]` |

Capital letters sort before small ones in `String` order, so the order of your steps
matters when the input mixes cases.
