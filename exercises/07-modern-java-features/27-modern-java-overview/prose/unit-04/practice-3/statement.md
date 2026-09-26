Java 17 added `RandomGenerator.JumpableGenerator`: `jump()` advances a
generator's state by a huge number of steps (`2^64` for `Xoroshiro128PlusPlus`),
to a statistically independent subsequence. The page uses it to hand out
independent chunks for parallel work: draw a chunk, then **jump**, then draw the
next chunk.

Write `Chunks.split(long seed, int chunks, int perChunk)`:

1. Create the `Xoroshiro128PlusPlus` generator with `seed`
   (`RandomGeneratorFactory.of("Xoroshiro128PlusPlus").create(seed)`); it is a
   `RandomGenerator.JumpableGenerator`.
2. For each chunk, in order: draw `perChunk` values with `ints(perChunk, 0, 100)`,
   then call `jump()`.
3. Return the chunks as a list of lists.

The **first** chunk therefore starts from the seeded state, and every later
chunk starts after one more jump.

| call | answer |
|---|---|
| `split(42, 3, 5)` | three lists of five values from 0 to 99 |
| `split(42, 3, 5).get(0)` | the first five values of the seeded generator |
| `split(42, 3, 5)` twice | equal results |
| `split(42, 0, 5)` | `[]` |
