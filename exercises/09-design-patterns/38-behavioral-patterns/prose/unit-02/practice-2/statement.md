The page's second pitfall: an observer that calls `removeObserver()` inside
`update()` changes the list the subject is looping over, and the loop fails
with `ConcurrentModificationException`. The fix is to **iterate a snapshot**
of the list. Its edge cases add one more rule: **one observer that throws
must not stop the others** from being notified.

`Observer<T>` (given) has `void update(String event, T data)`. Write
`SafeSubject<T>`:

- `addObserver(o)` registers `o` (`null` throws `IllegalArgumentException`);
  `removeObserver(o)` unregisters it.
- `notifyObservers(event, data)` calls `update(event, data)` on **the
  observers registered when the notification starts**. An observer may add
  or remove observers (itself included) from inside `update()`: that
  changes who gets the *next* event, not this one.
- If an observer throws a `RuntimeException`, the rest are still notified.
  `notifyObservers` returns the exceptions thrown, in the order they
  happened (an empty list when none).

| observers, in order | during `update` | first event reaches | second event reaches |
|---|---|---|---|
| A, R, B | R removes R | A, R, B | A, B |
| A, B | A adds N | A, B | A, B, N |
| X, B | X throws | B, and X's exception is returned | B, and X's exception again |
