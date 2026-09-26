A subtype must not **strengthen a precondition** (require more than the base type) and
must not **weaken a postcondition** (deliver less than the base type promises). The page's
`SortedCollection` states its contract in comments:

```java
interface SortedCollection<T extends Comparable<T>> {
    void add(T item);           // Precondition: item != null
    List<T> getAll();           // Postcondition: returned list is sorted
    int size();                 // Invariant: size() >= 0
}
```

In `Sorted`, write `SortedArrayList`, an implementation that keeps the whole contract:

- `add(item)` accepts **every** non-null item, duplicates included, and throws
  `NullPointerException` for `null` (the same precondition, no stronger);
- `getAll()` returns the items in natural order, as a list the caller cannot change: any
  attempt to change it (`add`, `set`, `remove`, `clear`, `sort`) throws
  `UnsupportedOperationException` and leaves the collection as it was;
- `size()` counts the items added.

Adding `3000`, `1000`, `2000` gives `[1000, 2000, 3000]`, and adding `2000` twice keeps
both. Items that compare as equal are all kept too, such as `new BigDecimal("2.0")` and
`new BigDecimal("2.00")`.
