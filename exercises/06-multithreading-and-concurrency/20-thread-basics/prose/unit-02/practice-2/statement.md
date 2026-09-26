Two pitfalls on this page pull in opposite directions. Reading a worker's result
without `join()` is a race: "results may be empty when we read it". But calling
`join()` straight after each `start()` makes the work sequential, with "no benefit
from threading". The fix is to **start every worker first, then join them all**.

Write `Fanout.mapAll(List<Integer> inputs, Function<Integer, Integer> f)`. Run
`f` on each input on its **own** new thread, start all of those threads before
joining any, and return the results **in the inputs' order** once every thread has
finished.

| inputs, `f` | result |
|---|---|
| `[3, 1, 4, 2]`, `x -> x * x` | `[9, 1, 16, 4]` |
| `[5]`, `x -> x + 1` | `[6]` |
| `[]`, any | `[]` |
