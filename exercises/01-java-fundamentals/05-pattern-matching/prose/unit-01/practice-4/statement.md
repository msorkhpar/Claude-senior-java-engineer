A chain of `instanceof` checks has to be edited every time a new type appears, and silently
ignores a type it does not know. Polymorphism puts the behaviour in each class instead:
every `Shape` computes its own `area()`, and code that uses shapes just calls it.

`Shapes` holds `interface Shape { double area(); }` and two records. Complete them:

1. `Circle.area()` is `π × radius²`: `new Circle(1).area()` is `Math.PI`.
2. `Rectangle.area()` is `width × height`: `new Rectangle(4, 6).area()` is `24.0`.
3. `total(List<Shape> shapes)` returns the sum of the areas, and must work for **any**
   `Shape`, including kinds added later that you have never seen, so it uses no
   `instanceof` at all.
