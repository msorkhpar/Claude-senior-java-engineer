Abstraction keeps what matters and hides the rest. Every shape can say its
area and its perimeter; **how** it computes them is its own business. The
lesson's abstract `Shape` declares exactly those two methods.

In `Figures`, implement three shapes behind that abstraction:

- `Circle(double radius)`: area `π·r²`, perimeter `2·π·r`.
- `Square(double side)`: area `side²`, perimeter `4·side`.
- `Triangle(double a, double b, double c)`: perimeter `a + b + c`; area by
  Heron's formula, `√(s·(s−a)·(s−b)·(s−c))` with `s` half the perimeter.

The constructors refuse bad input with `IllegalArgumentException`:

- a square's side, and a circle's radius, must be greater than 0;
- a triangle's sides must each be shorter than the sum of the other two,
  in any order. Sides that only **equal** that sum lie on one line and are
  refused too.

## Examples

```
new Circle(1).area()          -> 3.14159...   perimeter() -> 6.28318...
new Square(2).area()          -> 4.0          perimeter() -> 8.0
new Triangle(3, 4, 5).area()  -> 6.0          perimeter() -> 12.0
new Triangle(1, 2, 10)        -> IllegalArgumentException
new Triangle(10, 2, 1)        -> IllegalArgumentException
new Triangle(1, 2, 3)         -> IllegalArgumentException
new Square(0)                 -> IllegalArgumentException
```
