Inside a loop, `continue` skips the rest of the current pass and moves on to the next one,
while `break` ends the loop altogether.

Write `total(int[] readings)` in `Sentinel`. Walk the readings in order and add them up, but:

- a negative reading is a glitch: skip it and keep going;
- a `0` marks the end of the data: stop there, and ignore everything after it.

Examples: `total({3, 4, 5})` is `12`, `total({3, -1, 4})` is `7`,
`total({3, 4, 0, 10})` is `7`, and `total({0, 5})` is `0`.
