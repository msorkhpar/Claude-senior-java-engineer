Record patterns nest, so one pattern can take apart a record and the records inside it:

```java
if (rect instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) { ... }
```

`var` can stand for each component's type. A nested pattern such as `Point(var x, var y)`
does not match a `null` component, so a rectangle missing a corner simply does not match.

`Boxes` holds `record Point(int x, int y)` and `record Rectangle(Point topLeft, Point
bottomRight)`. Write `area(Object shape)` in `Boxes`:

- the area of a rectangle, whichever order its two corners come in:
  `Rectangle(Point(1, 2), Point(3, 5))` has area `6`, and so does
  `Rectangle(Point(3, 5), Point(1, 2))`;
- `-1` for anything else: something that is not a rectangle, `null`, or a rectangle with a
  `null` corner.
