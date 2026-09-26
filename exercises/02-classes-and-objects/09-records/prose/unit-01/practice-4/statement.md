A record may declare **static** members beside its components: constants,
static fields and static factory methods. (What it may not declare is an extra
*instance* field.)

Complete the record `Point(int x, int y)` from the page's interview answers:

- `static final Point ORIGIN`: the point `(0, 0)`, one shared constant;
- `static Point parse(String text)`: a factory reading `"x,y"`. Spaces around
  each number are allowed and numbers may be negative. Anything else (a
  missing comma, extra parts, a number that is not a whole number) is refused
  with an `IllegalArgumentException`, never a `NumberFormatException`;
- `double distanceFromOrigin()`: the straight-line distance from `ORIGIN`.

The page's own `Math.sqrt(x * x + y * y)` squares in `int` arithmetic, which
overflows once a coordinate passes 46 340. Coordinates here can be anywhere
in the `int` range, so do the arithmetic without overflow.

## Examples

```
Point.parse("3,4")                               -> Point[x=3, y=4]
Point.parse("3,4").distanceFromOrigin()          -> 5.0
Point.ORIGIN                                     -> Point[x=0, y=0]
Point.parse(" -3 , 4 ")                          -> Point[x=-3, y=4]
new Point(3_000_000, 4_000_000).distanceFromOrigin()  -> 5000000.0
Point.parse("3;4"), Point.parse("1,2,3"), Point.parse("a,1")  -> IllegalArgumentException
```
