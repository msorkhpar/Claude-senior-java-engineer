`Map.computeIfAbsent(key, function)` is "get or compute" in one call: it runs `function` only when `key` is missing,
stores the result and returns the value now in the map. It replaces the get-check-put dance.

Write two methods in `Groups`, each built on `computeIfAbsent`:

1. `byFirstLetter(List<String> words)` returns a map from a lower-case first letter to the words starting with it,
   in their original order.
2. `lengthOf(Map<String, Integer> cache, String word, Function<String, Integer> measure)` returns the cached value for
   `word`, measuring and caching it first if needed.

| call | result |
|---|---|
| `byFirstLetter(["apple", "banana", "avocado"])` | `{a=[apple, avocado], b=[banana]}` |
| `lengthOf({}, "hello", String::length)` | `5`, and the cache now holds `hello=5` |

Measuring can be expensive, so it must happen at most once per word. Words may start with a capital, and a word may
be empty.
