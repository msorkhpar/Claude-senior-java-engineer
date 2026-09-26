Reflection knows about records and sealed types:

- `isRecord()` and `getRecordComponents()`: each component's name and type,
  **in declaration order**.
- `isSealed()` and `getPermittedSubclasses()`: the types a sealed type
  permits. A permitted type **can be sealed itself**, so finding every
  concrete type of a hierarchy means going down again.

Write `ShapeCatalog`:

- `components(type)`: `"name: Type"` for each record component (simple type
  name), in declaration order. A type that is **not a record** is refused
  with `IllegalArgumentException`.
- `leaves(type)`: the simple names of every type reached through
  `getPermittedSubclasses()` that is **not sealed itself**, sorted. A type
  that is not sealed is refused with `IllegalArgumentException`.

| call | result |
|---|---|
| `components(Point.class)` for `record Point(int x, int y)` | `[x: int, y: int]` |
| `components(Span.class)` for `record Span(int end, int begin)` | `[end: int, begin: int]` |
| `leaves(Shape.class)`, `Shape permits Circle, Rect` | `[Circle, Rect]` |
| `leaves(Figure.class)`, `Figure permits Dot, Polygon`, `Polygon permits Square, Triangle` | `[Dot, Square, Triangle]` |
| `leaves(Runnable.class)` | `IllegalArgumentException` |
