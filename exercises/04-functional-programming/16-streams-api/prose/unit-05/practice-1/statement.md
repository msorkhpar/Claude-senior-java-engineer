A parallel `reduce` splits the stream into chunks, reduces each chunk from the identity, and combines the partial
results. That is only correct when the operation is **associative**, when the identity really is an identity
(`identity op x == x`), and, for the three-argument `reduce(identity, accumulator, combiner)`, when the combiner
merges two partial results the way the accumulator would have.

Write three static methods in `Totals`. Each receives a stream that may be sequential or parallel, and must give
the same answer either way:

1. `int balance(Stream<Integer> withdrawals, int opening)`: the opening balance minus every withdrawal.
2. `int withBonus(Stream<Integer> scores, int bonus)`: the sum of the scores plus the bonus, counted once.
3. `int totalLength(Stream<String> words)`: the total number of characters (the three-argument `reduce` is one way to write it).

| call | answer |
|---|---|
| `balance(Stream.of(10, 20, 30), 100)` | `40` |
| `withBonus(Stream.of(1, 2, 3), 10)` | `16` |
| `totalLength(Stream.of("a", "bb", "ccc"))` | `6` |

A version that only passes sequential streams is not done: the tests also hand it large parallel streams.
