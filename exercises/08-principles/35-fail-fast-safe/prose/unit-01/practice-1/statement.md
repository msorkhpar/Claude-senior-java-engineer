`ArrayList`, `HashMap` and the other `java.util` collections keep an `int` field, `modCount`,
that moves on every structural change. Their iterators copy it when they are created, and
check it on each iterator operation. The page shows the shape, simplified from `ArrayList`:

```java
private class Itr implements Iterator<E> {
    int expectedModCount = modCount; // snapshot at creation

    public E next() {
        checkForComodification(); // compare modCount == expectedModCount
        // ... return next element
    }

    public void remove() {
        checkForComodification();
        // ... remove element from underlying list
        expectedModCount = modCount; // re-sync after iterator's own modification
    }

    final void checkForComodification() {
        if (modCount != expectedModCount)
            throw new ConcurrentModificationException();
    }
}
```

`TinyList<E>` is a small growable list. Its `add`, `get`, `size` and `removeAt` are written,
and `add` and `removeAt` already move `modCount`. Write `iterator()`:

- it returns the elements in order; `hasNext()` is `false` at the end, and `next()` there
  throws `NoSuchElementException`;
- **the fail-fast check**: the iterator copies `modCount` when it is created. After any
  structural change made through the list, not through the iterator, the next `next()`
  throws `ConcurrentModificationException`. The check comes first in `next()`, before the
  end-of-list check, and it counts changes, not sizes: an `add` followed by a `removeAt`
  still trips it, and so does a change made before the first `next()`;
- **`remove()` keeps the iterator in step**: it removes the element `next()` last returned,
  and iteration then carries on with the element after it, with no exception. `remove()`
  makes the same check first: after an outside change it throws
  `ConcurrentModificationException` and removes nothing;
- `remove()` with no `next()` before it, or twice for one `next()`, throws
  `IllegalStateException` and removes nothing.
