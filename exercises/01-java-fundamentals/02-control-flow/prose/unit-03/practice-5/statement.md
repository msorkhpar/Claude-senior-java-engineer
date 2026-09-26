A loop must make progress toward a false condition. When you cannot be sure it will, a
safety counter stops it after a fixed number of passes.

The Collatz walk starts at `n` and repeats one step until it reaches `1`: an even value is
halved, an odd value `v` becomes `3 * v + 1`. Nobody has proved it always reaches `1`.

Write `steps(long n, int maxSteps)` in `Collatz`. It returns how many steps the walk from
`n` takes to reach `1`:

- `steps(6, 100)` is `8` (6, 3, 10, 5, 16, 8, 4, 2, 1), and `steps(1, 100)` is `0`;
- if the walk needs more than `maxSteps` steps, throw `IllegalStateException`:
  `steps(27, 100)` throws, because 27 needs 111 steps, while `steps(27, 111)` is `111`;
- `n` below `1` would never reach `1` (0 halves to 0 forever): throw
  `IllegalArgumentException` before the loop starts.
