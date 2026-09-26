Java 21's record patterns (JEP 440) deconstruct a record in a `switch` or an
`instanceof`: `case Point(int x, int y)` binds the components, and patterns
nest, so `case Line(Point(int x1, int y1), Point(int x2, int y2))` reaches into
both end points at once. Guards (`when`) refine them, and the first matching case
wins, so the **most specific guard comes first**.

`Segments` declares `record Point(int x, int y)` and
`record Line(Point start, Point end)`. Write `Segments.describe(Object obj)` as
one `switch` with record patterns, checked in this order:

| obj | answer |
|---|---|
| a `Line` whose start and end are the same point | `"degenerate at (x, y)"` |
| a `Line` with `x1 == x2` | `"vertical at x=" + x1` |
| a `Line` with `y1 == y2` | `"horizontal at y=" + y1` |
| any other `Line` | `"from (x1, y1) to (x2, y2)"` |
| `Point(0, 0)` | `"origin"` |
| any other `Point` | `"point (x, y)"` |
| anything else | `"not a shape"` |

For example, `describe(new Line(new Point(1, 2), new Point(1, 2)))` is
`"degenerate at (1, 2)"` and `describe(new Line(new Point(0, 0), new Point(3, 4)))`
is `"from (0, 0) to (3, 4)"`.
