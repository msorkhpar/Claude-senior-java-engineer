`RandomGeneratorFactory.of(name)` picks an algorithm by name, and
`create(seed)` makes a seeded generator. Two of the page's pitfalls:

- Hardcoding an algorithm name without checking it exists: `of()` throws
  for a name the JVM does not have. The page checks first with
  `RandomGeneratorFactory.all()`.
- **Confusing seeded and unseeded generators**: a generator created with a seed
  is **reproducible**, giving the same sequence for the same seed, while
  `create()` without a seed is not.

Write the class `Seeds`:

- `available(String algorithm)` returns whether the JVM has an algorithm with
  exactly that name (names are case-sensitive). It never throws.
- `sample(String algorithm, long seed, int count, int bound)` returns `count`
  values from `0` (inclusive) to `bound` (exclusive), drawn with `ints` from
  that algorithm's generator created with the whole `long` `seed`.

| call | answer |
|---|---|
| `available("L64X128MixRandom")` | `true` |
| `available("NoSuchAlgorithm")`, `available("")` | `false`, `false` |
| `sample("L64X128MixRandom", 42, 5, 100)` twice | the same five values both times |
| `sample("L64X128MixRandom", 1, 20, 1000)` and `sample("L64X128MixRandom", 2, 20, 1000)` | two different lists |
