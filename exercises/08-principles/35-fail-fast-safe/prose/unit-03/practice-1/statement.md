Immutable collections sidestep the fail-fast and fail-safe choice: nothing can change them, so
there is nothing to detect. The page warns that the two ways to hand one out are not alike:

```java
// Unmodifiable VIEW vs. immutable COPY
List<String> mutable = new ArrayList<>(List.of("a", "b"));
List<String> view = Collections.unmodifiableList(mutable);
List<String> copy = List.copyOf(mutable);

mutable.add("c");
System.out.println(view); // [a, b, c] -- reflects change!
System.out.println(copy); // [a, b] -- independent copy
```

`Roster` keeps its names in a private `ArrayList`, and `add` is written. Write:

- `snapshot()`: the names as they are now. **A snapshot keeps its contents**: names added later
  do not appear in it;
- `liveView()`: a read-only window on the names. **A live view follows the roster**: names added
  later appear in it;
- **neither can be changed by its holder**: every change on either, `add`, `remove` by index
  or by value, `set` or `clear`, throws `UnsupportedOperationException`, and the roster itself
  is unchanged.
