Java 17's `RandomGenerator` (JEP 356) is the common interface of every random
number generator: `Random`, `SecureRandom`, `SplittableRandom`,
`ThreadLocalRandom` and the new algorithms from `RandomGeneratorFactory`. The
page's advice: **take a `RandomGenerator` as the parameter type**, so callers
choose the algorithm, and a seeded one in tests. Its `ints(count, origin, bound)`
streams `count` values from `origin` (inclusive) to `bound` (**exclusive**), and
the page's edge cases say a **negative count** throws `IllegalArgumentException`.

Write `Dice.roll(RandomGenerator rng, int count, int sides)`. It returns `count`
rolls of a die with faces `1` to `sides` (inclusive), drawn from **`rng`**.

| call | answer |
|---|---|
| `roll(new Random(7), 3, 6)` | three values, each from 1 to 6 |
| `roll(RandomGeneratorFactory.of("L64X128MixRandom").create(42), 1000, 6)` | 1000 values, among which both 1 and 6 appear |
| `roll(new SplittableRandom(1), 0, 6)` | `[]` |
| `roll(new Random(7), -1, 6)` | throws `IllegalArgumentException` |
