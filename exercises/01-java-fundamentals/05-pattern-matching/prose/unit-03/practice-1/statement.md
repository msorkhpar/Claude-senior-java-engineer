A record pattern tests a type and takes the record apart in one step:

```java
if (obj instanceof Point(int x, int y)) {
    // x and y are the point's components
}
```

`Quadrants` holds `record Point(int x, int y)`. Write `quadrant(Object obj)` in `Quadrants`:

- for a point, the quadrant it lies in: `1` for `x > 0, y > 0`, `2` for `x < 0, y > 0`,
  `3` for `x < 0, y < 0` and `4` for `x > 0, y < 0`;
- `0` for a point on an axis (`x == 0` or `y == 0`);
- `-1` for anything that is not a point, `null` included.

Examples: `quadrant(new Quadrants.Point(2, 3))` is `1`, `quadrant(new Quadrants.Point(-2, -3))`
is `3`, `quadrant(new Quadrants.Point(0, 5))` is `0`, and `quadrant("2,3")` is `-1`.
