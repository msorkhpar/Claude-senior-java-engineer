`equals(Object o)` receives any object, `null` included. A pattern checks the type and
binds the other point in one step, and it is simply `false` for `null` or another type:

```java
return o instanceof Point p && ...;
```

Complete `Point`, a pair of `int` coordinates:

- `new Point(1, 2).equals(new Point(1, 2))` is `true`, and `equals(new Point(2, 1))` is
  `false`;
- `equals(null)` and `equals("1,2")` are `false`, and never throw;
- equal points have equal `hashCode()`s.
