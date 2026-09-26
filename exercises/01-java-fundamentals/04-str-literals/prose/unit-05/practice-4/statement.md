A builder's capacity is the room it has allocated; its length is how much text it holds.
`new StringBuilder()` starts with room for 16 characters and grows as needed;
`new StringBuilder(n)` starts with room for `n`, which saves regrowing when the final length
is known. A negative capacity throws `NegativeArraySizeException`, a poor message for a
caller's mistake.

Write `forLength(int expectedLength)` in `Capacity`. It returns an empty `StringBuilder`
whose capacity is exactly `expectedLength`:

- `forLength(20).capacity()` is `20`, and its `length()` is `0`;
- `forLength(0).capacity()` is `0`;
- a negative `expectedLength` throws `IllegalArgumentException`.
