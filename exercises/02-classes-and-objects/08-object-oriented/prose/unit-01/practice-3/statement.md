An **abstract class** cannot be instantiated. It may declare abstract methods
(no body) that every concrete subclass must implement, and it may still have a
constructor, fields and ordinary methods that its subclasses share.

`Shapes` holds the lesson's own hierarchy: an abstract `Shape` with a colour,
and two concrete subclasses. Write:

- `Shape(String color)` stores the colour; `displayColor()` returns
  `"The shape color is <color>"`.
- `Circle(String color, double radius)` and
  `Rectangle(String color, double width, double height)` pass the colour to
  `Shape`, keep their own dimensions, and implement `calculateArea()`
  (`π·r²` and `width·height`).
- A negative radius, width or height is refused with `IllegalArgumentException`
  (zero is allowed).
- Each subclass overrides `toString()` so it names its kind and **all** of its
  fields, colour included, e.g. `Circle{color='Red', radius=5.0}` and
  `Rectangle{color='Blue', width=4.0, height=5.0}`.
- `Shapes.totalArea(List<? extends Shape> shapes)` returns the sum of the
  areas; an empty list has a total of `0.0`.

## Examples

```
new Circle("Red", 5.0).calculateArea()          -> 78.539...
new Rectangle("Blue", 4.0, 5.0).calculateArea() -> 20.0
totalArea([Circle("Green", 3.0), Rectangle("Yellow", 2.0, 3.0)]) -> 34.274...
new Circle("Red", 5.0).displayColor()           -> "The shape color is Red"
new Circle("Red", 5.0).toString()               -> "Circle{color='Red', radius=5.0}"
totalArea([])                                   -> 0.0
new Circle("Red", -1.0)                         -> IllegalArgumentException
```
