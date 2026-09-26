The page's `RaceOnCheckThenAct.getOrCompute` checks `containsKey` and then calls
`put`: two threads can both see the key absent, and both compute and insert it.
Its fix makes the check and the act one atomic step with
`ConcurrentHashMap.computeIfAbsent`.

Write `ComputeCache`. Its constructor takes the (possibly expensive) function that
computes a value from a key. `get(key)` returns the key's value, computing it only
the first time; `size()` says how many keys are cached.

Keys are compared by their text (`equals`), as in any map.

**The rule:** each key's function runs **at most once**, even when several
threads ask for the same missing key at the same moment.

| calls | answer | function runs |
|---|---|---|
| `get("a")` on a new cache | `"A"` (for `String::toUpperCase`) | once |
| `get("a")` again | `"A"` | not again |
| `get("b")`, then `size()` | `2` | once for `"b"` |
| `get("ab")`, then `get(new String("ab"))` | `"AB"` both times | once |
| two threads call `get("x")` together | both get `"X"` | once |
