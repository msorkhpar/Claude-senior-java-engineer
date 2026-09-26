`Shapes` holds the course's sealed hierarchy: an abstract `Shape` that
permits exactly `Circle`, `Square` and `Triangle`. `Circle` and `Square` are
final; `Triangle` is `non-sealed`, and the file already extends it with a
`RightTriangle`.

Write `static String describe(Shape shape)` as the page's interview answer
does: one `switch` expression with a type pattern per permitted subclass and no
`default`. Build the text from each shape's own getters:

| shape | text |
|---|---|
| `Circle` | `A circle with radius <radius>` |
| `Square` | `A square with side length <side>` |
| `Triangle` | `A triangle with base <base> and height <height>` |

Numbers are printed the way string concatenation prints a `double`.
Every kind of triangle is a triangle.

**Examples**

- `describe(new Circle(5))` -> `"A circle with radius 5.0"`
- `describe(new Square(4))` -> `"A square with side length 4.0"`
- `describe(new Triangle(3, 4))` -> `"A triangle with base 3.0 and height 4.0"`
