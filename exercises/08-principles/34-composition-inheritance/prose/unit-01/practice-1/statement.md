The page's classic bug: a subclass of `HashSet` counts the elements added to it, and gets
the count wrong.

```java
public class InstrumentedHashSet<E> extends HashSet<E> {
    private int addCount = 0;

    @Override
    public boolean add(E e) {
        addCount++;
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        addCount += c.size();
        return super.addAll(c); // BUG: HashSet.addAll calls add() internally!
    }
}
```

`HashSet.addAll` calls `add` for each element, so every element is counted twice. That is
the **fragile base class problem**: the subclass depends on how its superclass is
implemented, not only on its contract.

The fix is **composition**: a `ForwardingSet` holds any `Set` and forwards each call to it,
and `InstrumentedSet` extends the forwarding class, not `HashSet`. The starter gives you
`ForwardingSet` with the `Set` methods already forwarded. Finish it:

- `InstrumentedSet.add` and `addAll` count every element offered (duplicates included), and
  `getAddCount()` returns the count. `add("a")`, `add("b")`, `add("a")` gives a count of 3
  and a set of 2, and `addAll` of three elements adds exactly 3. A duplicate offered to
  `addAll` counts too: `add("a")` then `addAll(List.of("a", "b"))` gives a count of 3;
- the wrapper is a **view** of the set it wraps: what is added through it lands in that set,
  and what is added to that set is seen through it;
- `ForwardingSet` also forwards `equals`, `hashCode` and `toString`, so the wrapper equals
  any set with the same elements, and such a set equals the wrapper;
- the constructor refuses a `null` set with a `NullPointerException` (`Objects.requireNonNull`).
