The page's `Planet` enum is its model of parameterized constants: each constant
is built with its own mass (kg) and radius (m), and derives the rest from them.
Behavior lives with the data, and `values()` lists every planet for free.

The constants, their fields and `G = 6.67300E-11` are given. Write:

- `double surfaceGravity()`: `G * mass / (radius * radius)`.
- `double surfaceWeight(double otherMass)`: `otherMass * surfaceGravity()`.
- `static Planet strongest(Collection<Planet> among)`: the planet with the
  highest surface gravity among `among` (throw `NoSuchElementException` when
  it is empty).
- `static List<Planet> withGravityBetween(double min, double max)`: every
  planet whose surface gravity is between `min` and `max`, both bounds
  included, in declaration order.

| call | answer |
|---|---|
| `EARTH.surfaceGravity()` | about `9.80` |
| `EARTH.surfaceWeight(75)` | about `735.2` |
| `strongest(List.of(URANUS, SATURN, NEPTUNE))` | `NEPTUNE` |
| `withGravityBetween(9, 12)` | `[EARTH, SATURN, NEPTUNE]` |
| `withGravityBetween(8, 12)` | `[VENUS, EARTH, SATURN, URANUS, NEPTUNE]` |
