Unless you memoize, every `get()` re-runs a Supplier's lambda. A memoizing wrapper turns "produce this value when
asked" into "produce it once, then hand back the cached result".

Write `Memo.memoize(Supplier<T> source)`. The returned supplier calls `source` on its first `get()` and returns that
same value on every later `get()` without calling `source` again.
(As background, the page wraps the check-and-compute in `synchronized` so concurrent callers compute once too; the
tests here call `get()` from a single thread.)

| calls on `memoize(() -> new Object())` | source runs | result |
|---|---|---|
| first `get()` | yes | a new object |
| second `get()` | no | the same object |

Think about a source whose honest answer is `null`, and about a source that throws the first time it is asked.
