A fail-fast iterator trips when the collection's `modCount` has moved since the iterator was
created. The page draws the line that decides what moves it:

> Only structural changes (add, remove, clear, resize) increment `modCount`. Changing a value in
> a `Map` via `put()` for an existing key or calling `set()` on a `ListIterator` are NOT structural
> modifications and do NOT trigger the exception.

`CountedList<E>` wraps an `ArrayList` and keeps its own `modCount`, and its fail-fast
`iterator()` is written. Write its four writes, `add`, `remove(int)`, `set(int, E)` and
`clear()`, each doing what the `ArrayList` method of that name does, so that:

- **each structural change moves `modCount` by one**: `add`, `remove` and `clear`;
- **`set` is not structural**: it replaces a value in place and leaves `modCount` as it was, so
  a traversal may `set` every element it visits without an exception;
- after an `add`, a `remove` or a `clear` in the middle of a traversal, the iterator's next
  `next()` throws `ConcurrentModificationException`;
- `remove(int)` and `set(int, E)` work by position, as `ArrayList`'s do, even when the list
  holds equal elements elsewhere; a `remove` whose index is out of range throws
  `IndexOutOfBoundsException` and changes nothing, `modCount` included;
- `clear()` always counts as one structural change, even on an empty list, as `ArrayList`'s does.
