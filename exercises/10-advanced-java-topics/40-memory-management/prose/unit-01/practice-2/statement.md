Every method call pushes a **stack frame**, and each thread's stack is small
(about 1 MB by default). A recursion as deep as the data, one frame per node,
throws `StackOverflowError` on long inputs; the page's fix is to **convert deep
recursion to iteration**.

`Chain.Node` (given) is a record `(int value, Node next)`; `null` ends a chain.
Write:

- `sum(head)` and `length(head)`: **the chain is walked with a loop, not
  recursion**, so a chain of a million nodes works; **`null` is an empty
  chain** (sum 0, length 0).
- `factorial(n)` from the page's pitfall: **a negative input is refused** with
  `IllegalArgumentException`, and **overflow is refused, not wrapped**: a
  result beyond `long` throws `ArithmeticException`.

| call | result |
|---|---|
| `sum(3 -> 4 -> 5)`, `length(...)` | 12, 3 |
| `sum` of 1,000,000 nodes of value 1 | 1000000 |
| `sum(null)` | 0 |
| `factorial(5)`, `factorial(0)` | 120, 1 |
| `factorial(-1)` | `IllegalArgumentException` |
| `factorial(21)` | `ArithmeticException` |
