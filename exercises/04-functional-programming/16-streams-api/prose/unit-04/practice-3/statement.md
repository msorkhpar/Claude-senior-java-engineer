`Collectors.toMap(keyMapper, valueMapper)` builds a map from a stream, but it throws `IllegalStateException` the
moment two elements produce the same key. The three-argument form takes a merge function for that case, and the
four-argument form also takes the map to fill.

Write `LengthIndex.byLength(List<String> words)`. It returns a map from each word length to the words of that
length, joined with `", "` in list order. The map's keys come in the order each length first appears in the list.

| words | answer |
|---|---|
| `["apple", "avocado", "blueberry"]` | `{5=apple, 7=avocado, 9=blueberry}` |
| `["apple", "banana", "cherry", "avocado", "blueberry"]` | `{5=apple, 6=banana, cherry, 7=avocado, 9=blueberry}` |
| `["blueberry", "apple"]` | `{9=blueberry, 5=apple}` |

Mind the collisions, the order of the keys, and the order of the words inside one value.
