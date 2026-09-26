Record patterns are also `case` labels, so one switch expression can take apart several
record types. `case null` gives the `null` selector its own answer.

`Shapes` holds `Point(int x, int y)`, `Rectangle(Point topLeft, Point bottomRight)` and
`Circle(Point center, int radius)`. Write `describe(Object shape)` in `Shapes` as one switch
expression, following the course's own `describeShape`:

| shape | result |
|---|---|
| `Rectangle(Point(1, 2), Point(3, 4))` | `"Rectangle from (1,2) to (3,4)"` |
| `Circle(Point(0, 0), 5)` | `"Circle at (0,0) with radius 5"` |
| `null` | `"Null shape"` |
| anything else | `"Unknown shape"` |
