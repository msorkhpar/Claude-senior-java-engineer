Composition is the default, not a ban on inheritance. Inheritance fits when there is a
genuine is-a relationship **and the superclass is designed and documented for extension**.
`AbstractList` is such a class: implement `get` and `size`, and it provides every other
`List` method from them, including throwing `UnsupportedOperationException` from the
methods that change a list. The page's example:

```java
public class ImmutableArrayList<E> extends AbstractList<E> {
    private final E[] elements;
    // ...
    @Override public E get(int index) { return elements[index]; }
    @Override public int size() { return elements.length; }
}
```

Write `ImmutableArrayList<E>`, which extends `AbstractList<E>`:

- the constructor takes any `Collection` and keeps its elements in order, `null` elements
  included;
  `new ImmutableArrayList<>(List.of("red", "green", "blue"))` reads as that list, and equals
  any list with the same elements;
- it cannot be changed by any route: `add`, `set`, `remove`, `sort`, `replaceAll`, and
  `remove` or `set` through its iterators all throw `UnsupportedOperationException`
  (extending a concrete class such as `ArrayList` would inherit working mutators instead);
- it keeps its own copy of the elements, so later changes to the source collection do not
  show in it, and changing an array that `toArray()` returned does not change it either.
