Changing a list while something traverses it is the fail-fast trap, and a stream is a traversal
too. The page's pitfall and fix:

```java
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
// WRONG -- modifying source during stream processing
list.stream().forEach(item -> {
    if ("b".equals(item)) list.remove(item); // ConcurrentModificationException (at the end)!
});

// FIX -- use removeIf or collect to new list
list.removeIf(item -> item.equals("b"));
```

When other code still holds the source, the page prefers the second way, a new collection:
"Streams offer a safe alternative to manual iteration + modification by collecting results into a
new collection", and `Collectors.toUnmodifiableList()` "to produce safe results from streams".

Write `longWords(List<String> source, int minLength)` in `Filters`. It returns the words of at
least `minLength` characters, in their order:

- `["a", "abc", "ab", "abcd"]` with `3` gives `["abc", "abcd"]`;
- **the source is left untouched**, whatever it holds;
- words are kept exactly as they are, duplicates and order included: nothing is trimmed,
  sorted or de-duplicated, and every character counts towards the length;
- **the result is a new list, and unmodifiable**: it is never the source itself, even when every
  word qualifies, and `add`, `set` or `remove` on it throws `UnsupportedOperationException`.
