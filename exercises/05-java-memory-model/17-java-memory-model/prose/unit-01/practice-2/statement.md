The page's safe-publication answer (Q5) rests on two tools. **Final fields**: a thread that
gets a reference to an object after its constructor finished sees the constructor's values
of its final fields, and of objects reachable through them, as of the end of the
constructor. **A volatile field**: storing a reference in it publishes the object to every
thread that reads the field later. Its `ImmutablePoint` and `SafePublisher` combine them.

Complete `SafePublication`, a small version of that example:

- `Point(int x, int y)` with `x()`, `y()` and `moved(int dx, int dy)`, which returns a
  **new** point and leaves this one as it was.
- `Polygon(List<Point> points)` with `points()`. A polygon never changes after its
  constructor: a later change to the caller's list does not reach it, and the list
  `points()` returns cannot be modified (it throws `UnsupportedOperationException`).
- `Publisher` with `publish(Polygon)` and `current()`, which returns the last polygon
  published, or `null` before the first.

Keep every piece of state in `private final` fields, and publish through a `volatile`
field, as the page does.

| call | answer |
|---|---|
| `new Point(1, 2).moved(3, 4)` | a point `(4, 6)`; the original is still `(1, 2)` |
| `new Publisher().current()` | `null` |
| `publish(new Polygon(List.of(new Point(0, 0), new Point(1, 1))))`, then `current().points()` | `[(0, 0), (1, 1)]` |
| `list.add(p)` on the caller's list after `new Polygon(list)` | the polygon still has its old points |
