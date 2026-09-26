The page asks: *can you have a race condition without a data race?* Yes. On a
`Collections.synchronizedSet`, `contains` and `add` are each synchronized, so
there is no data race, but `if (!set.contains(x)) set.add(x)` is still a
check-then-act: between the check and the add, another thread may add `x`.

Write `NameRegistry`. Its constructor takes the set to keep names in; the set is
thread-safe call by call (like `Collections.synchronizedSet`). `register(name)`
returns `true` only for the call that registered the name first, and `false` for
every later one. `isRegistered(name)` says whether it is in. Two names are the same name when their text is equal (`equals`), whichever `String` objects carry them.

| calls | answer |
|---|---|
| `register("ada")` | `true` |
| `register("ada")` again | `false` |
| `isRegistered("ada")`, `isRegistered("bob")` | `true`, `false` |
| `register(new String("ada"))` after `register("ada")` | `false` |
| two threads call `register("eve")` at the same moment | one gets `true`, the other `false` |

The tests hand you a set whose `add` can be held open, to make two registrations
meet in the middle.
