The lesson warns against **overusing inheritance**: prefer composition when
there is no clear "is-a" relationship, because a subclass is tied to how its
superclass works inside.

Write `CountingSet<E>`, a set that also counts how many elements anyone has
**tried** to add, duplicates included. Build it by **composition**: keep a
`Set<E>` in a private field and forward to it. Do not extend a set class: in
`HashSet`, `addAll` is implemented by calling `add`, so a subclass that counts
in both would count twice.

- `boolean add(E e)`: counts one attempt; returns what the set's `add` returns.
- `boolean addAll(Collection<? extends E> c)`: counts one attempt per element
  of `c`; returns `true` if the set changed.
- `int getAddCount()`: the number of attempts so far.
- `boolean contains(Object o)` and `int size()`: as the set answers them.

## Examples

```
add("a"); add("b"); add("c")    -> getAddCount() == 3, size() == 3
addAll(List.of("x", "y", "z"))  -> getAddCount() == 3
add("a"); add("a")              -> second add returns false, getAddCount() == 2, size() == 1
```
