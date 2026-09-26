A reflective factory maps names to classes and creates instances on demand,
so a new shape needs a registration, not a new `switch` branch. Two habits
make it robust: **validate at registration** (find the no-arg constructor
then, and keep it) rather than when the first `create` fails, and compare
names ignoring case **with `Locale.ROOT`**: `toLowerCase()` uses the default
locale, and in Turkish `"CIRCLE".toLowerCase()` is `"cırcle"`.

Write `ShapeFactory` for the nested interface `ShapeFactory.Shape` (given):

- `register(name, type)`: remembers `type` under `name`, ignoring case. A
  `type` with **no no-arg constructor** is refused at once with
  `IllegalArgumentException`.
- `create(name)`: a **new** instance of the class registered under `name`
  (any case). An unknown name is `IllegalArgumentException`.

| calls | result |
|---|---|
| `register("circle", Circle.class)`, `create("circle").area()` | `4π` |
| `register("square", Square.class)`, `create("Square").area()` | `16.0` |
| `register("CIRCLE", Circle.class)` in a Turkish default locale, `create("circle")` | a circle |
| `register("sized", Sized.class)`, `Sized` has only `Sized(double)` | `IllegalArgumentException` |
| `create("hexagon")` | `IllegalArgumentException` |
