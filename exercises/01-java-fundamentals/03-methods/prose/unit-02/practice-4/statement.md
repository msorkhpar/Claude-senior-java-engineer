A method that returns a collection returns an empty one when it has nothing to give, never
`null`, so its callers can loop over the result without a null check.

Write `first(int count)` in `EvenNumbers`. It returns a new, modifiable list of the first
`count` even numbers, starting from `0`:

- `first(3)` is `[0, 2, 4]`, and `first(1)` is `[0]`;
- `first(0)` and `first(-5)` are empty lists, not `null`;
- every call returns a list of its own: a caller may add to it.
