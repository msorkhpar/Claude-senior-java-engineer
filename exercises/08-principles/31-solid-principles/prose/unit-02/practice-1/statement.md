The page's pitfall is an area calculation that switches on a type name, so **every new
shape means editing it**:

```java
double calculateArea(String type, double... dims) {
    return switch (type) {
        case "circle" -> Math.PI * dims[0] * dims[0];
        case "rect" -> dims[0] * dims[1];
        default -> throw new IllegalArgumentException("Unknown: " + type);
    };
}
```

Make it open for extension and closed for modification. In `Areas`:

- `Shape` is the extension point: `area()` and `name()`.
- `Circle(radius)`, `Rectangle(width, height)` and `Triangle(base, height)` are records
  that compute their own area (`Math.PI * r * r`, `w * h`, `0.5 * b * h`) and name
  themselves `"Circle"`, `"Rectangle"` and `"Triangle"`.
- `AreaCalculator.totalArea(shapes)` adds up the areas, and `areaByType(shapes)` sums them
  per `name()`. It must work, **unchanged**, for a `Shape` written after it, such as the
  page's `Triangle` was.

The page's edge cases hold: no shapes total `0.0` (and an empty map), a zero-sized shape
is valid with area `0`, and a negative dimension is rejected with
`IllegalArgumentException` when the shape is created (`NaN` is not among the inputs). The tests
grade that a new shape needs no change to the calculator; how the calculator reaches the built-in
shapes' areas is the page's lesson, not graded beyond that.

For example, `Circle(1)`, `Rectangle(2, 3)` and `Triangle(4, 5)` total `PI + 16`, and
a new shape written later, a `Square(3)` that names itself `"Box"`, next to
`Rectangle(2, 3)` totals `15` and groups under `"Box"`. Every dimension of a shape is
checked, not only the first.
