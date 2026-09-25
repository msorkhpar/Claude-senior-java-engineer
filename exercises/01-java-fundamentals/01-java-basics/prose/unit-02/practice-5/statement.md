A **sealed** class names, in its `permits` clause, the only classes that may
extend it. Each permitted subclass must say how the hierarchy goes on below it:
`final` (no subclasses), `sealed` (its own `permits` list) or `non-sealed`
(open again). In return the compiler knows every kind of `Shape`, so a `switch`
over one needs no `default` branch.

The starter declares `Shapes.Shape` and its three subclasses as ordinary
classes. Make it the page's hierarchy and finish it:

1. `Shape` is an abstract **sealed** class that permits exactly `Circle`,
   `Square` and `Triangle`.
2. `Circle` and `Square` are `final`; `Triangle` is `non-sealed`, so other code
   may extend it.
3. `area()` returns each shape's area: `PI * r * r`, `side * side`, and
   `0.5 * base * height`.
4. `Shapes.describe(Shape)` uses a `switch` with type patterns to return
   `"circle of radius 2.0"`, `"square of side 4.0"` or `"triangle of area 6.0"`.
