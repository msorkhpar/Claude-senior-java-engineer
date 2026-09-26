The page advises to **favour composition over inheritance**, and warns that
inheritance can break encapsulation by depending on a superclass's internal
details. A classic case: a `HashSet` subclass that counts in both `add` and
`addAll` counts some elements twice, because `HashSet.addAll` happens to call
`add` internally, a detail the subclass never sees.

Write `CountingSet<E>` so that it **holds** a set instead of extending one:

- `CountingSet(Set<E> inner)` wraps the given set; every element you add goes into it;
- `boolean add(E e)` and `boolean addAll(Collection<? extends E> c)` behave like the
  wrapped set's, and return what it returns;
- `boolean contains(Object o)` and `int size()` answer from the wrapped set;
- `int addCount()` is how many elements callers have **tried** to add, including
  duplicates: `add` counts 1, `addAll` counts the size of its collection.

**Examples**

```
s = new CountingSet<>(new HashSet<>())
s.add("a"); s.add("a")          -> addCount() == 2, size() == 1
s.addAll(List.of("x", "y", "z"))  -> addCount() == 5
```
