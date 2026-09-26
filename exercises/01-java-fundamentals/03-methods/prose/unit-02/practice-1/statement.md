Java passes every argument by value. For an object, the value is a copy of the reference:
the method and the caller point to the same object, so a change to the object is seen by
the caller, while assigning a new object to the parameter only changes the method's copy.

```java
void modifyValues(int x, StringBuilder sb) {
    x = 10;                         // the caller's int is unchanged
    sb.append(" World");            // the caller sees this
    sb = new StringBuilder("New");  // the caller does not see this
}
```

Write `addAll(List<String> target, String... items)` in `Collector`. It adds every
non-null item to the caller's own `target` list, in order, and returns how many it added:

- with `target = ["a"]`, `addAll(target, "b", "c")` returns `2`, and `target` is then
  `["a", "b", "c"]`;
- `null` items are skipped: `addAll(target, "x", null, "y")` returns `2`;
- with no items, `addAll(target)` returns `0`.
