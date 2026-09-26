The course's `sumOfNumbers(n)` adds `1 + 2 + ... + n` with a counting loop:
initialization `int i = 1`, condition `i <= n`, update `i++`.

Write `sum(int n)` in `Triangle` with a `for` loop. It returns `1 + 2 + ... + n` as a `long`:

- `sum(5)` is `15`, and `sum(1)` is `1`;
- a loop whose condition is false at the start runs no pass, so `sum(0)` and `sum(-3)` are `0`;
- a large `n` must not overflow: `sum(100000)` is `5000050000`, which does not fit in an `int`.
