Records work well with **pattern matching**: a record pattern such as
`Line(Point(var x1, var y1), Point(var x2, var y2))` tests the type and pulls
the components out in one step, nested records included.

`Geometry` declares three records:

```java
record Point(int x, int y) {}
record Line(Point from, Point to) {}
record Box(Point corner, Point opposite) {}
```

Write `static int gridLength(Object shape)`, which measures a shape in grid
steps (moving only horizontally or vertically):

- a `Point` measures `0`;
- a `Line` measures `|x2 − x1| + |y2 − y1|`;
- a `Box` measures its perimeter, `2 · (|width| + |height|)`, where the two
  corners may be any two opposite corners;
- `null`, and any object that is none of these, measures `-1`.

Use a `switch` with record patterns.

## Examples

```
gridLength(new Point(2, 9))                                -> 0
gridLength(new Line(new Point(0, 0), new Point(3, 4)))     -> 7
gridLength(new Line(new Point(3, 4), new Point(0, 0)))     -> 7
gridLength(new Box(new Point(0, 0), new Point(2, 3)))      -> 10
gridLength("a circle")                                     -> -1
gridLength(null)                                           -> -1
```
