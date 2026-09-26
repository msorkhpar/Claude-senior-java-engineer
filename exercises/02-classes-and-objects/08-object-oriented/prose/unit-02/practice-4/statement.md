An **interface** is a contract: implementing classes supply its abstract
methods, and code written against the interface works with any of them.
Since Java 8 an interface may also carry **default** methods, whose body
implementers inherit and may override; a default method can call the
interface's abstract methods. Records can implement interfaces too.

This is the course's own `Shape2` design. In `Areas`, write:

- `Shape.display()`, a default method returning
  `"This is a shape with area: <area>"`, the area formatted with
  `String.format("%.2f", area)`.
- `Circle(double radius)` and `Rectangle(double length, double width)`
  implement `calculateArea()` (`π·r²`, `length·width`) and keep the default
  `display()`.
- The record `Square(double side)` implements `calculateArea()` and overrides
  `display()` to return `"A square with side <side>"`, the side formatted with
  `%.2f`.
- `Areas.largest(List<? extends Shape> shapes)` returns the shape with the
  largest area, or `Optional.empty()` when there is none.

(`String.format` uses your default locale; the tests run with a `.` decimal
separator.)

## Examples

```
new Circle(5).calculateArea()       -> 78.539...
new Rectangle(4, 5).calculateArea() -> 20.0
new Rectangle(4, 5).display()       -> "This is a shape with area: 20.00"
new Circle(1).display()             -> "This is a shape with area: 3.14"
new Square(3).display()             -> "A square with side 3.00"
largest([Circle(3), Rectangle(3, 4), Square(2)]) -> Circle(3)   (28.27 beats 12.0 and 4.0)
largest([])                         -> Optional.empty()
```
