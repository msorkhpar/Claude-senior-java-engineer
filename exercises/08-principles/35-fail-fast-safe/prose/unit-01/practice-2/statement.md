Removing through the collection inside a for-each loop is the classic fail-fast bug. The page's
wrong version and its two fixes:

```java
// WRONG -- usually throws ConcurrentModificationException (and if "remove-me" is the
// second-to-last element, the loop just ends early and silently skips the last one)
for (String item : list) {
    if (item.equals("remove-me")) {
        list.remove(item);
    }
}

// FIX -- use Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("remove-me")) {
        it.remove();
    }
}

// BETTER FIX (Java 8+) -- use removeIf
list.removeIf(item -> item.equals("remove-me"));
```

Write `removeAll(List<String> items, String target)` in `Purge`. It removes every element equal
to `target` from the caller's list, in place, and keeps the order of the rest. `target` is
never `null`, but the list may hold `null` elements, which stay.

- `["keep", "drop", "keep2", "drop"]` with `"drop"` becomes `["keep", "keep2"]`;
- matches next to each other are all removed: `["drop", "drop", "keep"]` becomes `["keep"]`;
- elements are compared by content, so a `"drop"` built at run time is removed too;
- only an exact match goes: `"Drop"` and `"drop "` (other case, a trailing space) stay, and so
  does a different string that happens to share the target's hash code;
- `["a", null, "drop"]` becomes `["a", null]`.
