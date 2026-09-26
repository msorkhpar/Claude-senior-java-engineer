The enhanced for loop runs on an iterator, so `list.add(...)` inside it trips the fail-fast
check, as in the course's demo:

```java
ListIterator<String> listIt = list.listIterator();
while (listIt.hasNext()) {
    listIt.next();
    list.add("new"); // Throws ConcurrentModificationException
}
```

A change made through the iterator itself is safe: `ListIterator.add` inserts the element just
before the cursor, so the traversal carries on with the next original element and never visits
what it inserted.

Write `expand(List<String> list, String suffix)` in `Echo`. It inserts, right after each element
of the caller's list, a copy of that element with `suffix` appended, in place.

- `["a", "b"]` with `"-x"` becomes `["a", "a-x", "b", "b-x"]`, and `[]` stays `[]`;
- every original element gets its echo, even one that already ends with the suffix, and each
  duplicate gets its own: `["a-x", "b"]` becomes `["a-x", "a-x-x", "b", "b-x"]`, and
  `["a", "a-x", "a"]` becomes `["a", "a-x", "a-x", "a-x-x", "a", "a-x"]`.
