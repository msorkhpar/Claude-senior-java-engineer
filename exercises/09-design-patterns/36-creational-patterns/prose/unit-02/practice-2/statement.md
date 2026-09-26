A **Simple Factory** is one static method that picks the product from a
parameter. The page's pitfall is a factory that hides a mistake: an **unknown
type must throw a descriptive exception**, never fall back to some default
product. Its products are records under a sealed interface.

`Shape` (given) is sealed with the records `Circle(double radius)`,
`Square(double side)` and `Triangle(double base, double height)`, each with
`area()`. Write:

- `Shapes.create(String type, double size)`: `"circle"` gives a circle of that
  radius, `"square"` a square of that side, `"triangle"` a triangle whose base
  and height are both `size`. Anything else throws `IllegalArgumentException`
  whose message contains the type.
- `Shapes.describe(Shape shape)` with a pattern `switch`:
  `Round r=<radius>`, `Square s=<side>`, `Triangle b=<base> h=<height>`.

| call | result |
|---|---|
| `create("circle", 2)` | `Circle[radius=2.0]`, area `12.566...` |
| `create("square", 3)` | `Square[side=3.0]`, area `9.0` |
| `create("triangle", 4)` | `Triangle[base=4.0, height=4.0]`, area `8.0` |
| `create("hexagon", 1)` | `IllegalArgumentException` mentioning `hexagon` |
| `create("Circle", 1)` | `IllegalArgumentException` mentioning `Circle` |
| `describe(new Square(3))` | `Square s=3.0` |
