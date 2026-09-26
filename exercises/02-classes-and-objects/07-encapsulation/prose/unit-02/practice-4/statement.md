A getter need not read a field: a **derived property** is calculated from other
fields when it is asked for. Getters follow the JavaBeans names: `getX()`, or
`isX()` for a `boolean`. Setters are `setX(...)` and validate what they store.

Write `Rectangle`:

- `Rectangle(double width, double height)`;
- `getWidth()`, `getHeight()`, `setWidth(double)`, `setHeight(double)`: a side must
  be greater than `0`; otherwise throw `IllegalArgumentException` and keep the old
  value (the constructor refuses the same way);
- `double getArea()`: width times height;
- `boolean isSquare()`: whether the two sides are equal.

Examples:

```
Rectangle r = new Rectangle(3, 4);
r.getArea()       -> 12.0
r.isSquare()      -> false
r.setWidth(4);
r.getArea()       -> 16.0
r.isSquare()      -> true
r.setHeight(0)    -> IllegalArgumentException
```
