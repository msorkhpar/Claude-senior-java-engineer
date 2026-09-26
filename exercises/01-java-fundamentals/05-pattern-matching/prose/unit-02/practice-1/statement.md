Pattern matching for `instanceof` tests a type and binds a variable of that type in one
step, so no cast follows:

```java
if (obj instanceof Square square) {
    // use square directly
}
```

`Areas` holds three records: `Square(side)`, `Circle(radius)` and `Rectangle(width, height)`.
Write `area(Object obj)` in `Areas` with one `instanceof` pattern per shape:

- `area(new Areas.Square(3))` is `9.0`, `area(new Areas.Circle(1))` is `Math.PI`, and
  `area(new Areas.Rectangle(2, 5))` is `10.0`;
- anything else, `null` included, is not a shape: throw `IllegalArgumentException`.
