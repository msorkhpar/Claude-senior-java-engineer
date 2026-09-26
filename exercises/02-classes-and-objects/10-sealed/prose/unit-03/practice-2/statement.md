The page suggests sealed types for state machines: only the valid states
exist, and the transitions between them are controlled. `Door` models a door
whose state is one of three records of a sealed interface `State`:
`Opened()`, `Closed()` and `Locked(String code)`.

Write the four transitions. Each takes the current state and returns the next
one:

| transition | from `Opened` | from `Closed` | from `Locked(c)` |
|---|---|---|---|
| `open(s)` | `Opened` | `Opened` | refused |
| `close(s)` | `Closed` | `Closed` | unchanged |
| `lock(s, code)` | refused | `Locked(code)` | unchanged |
| `unlock(s, code)` | unchanged | unchanged | `Closed` if `code` equals `c`, else unchanged |

A code matches when its text is the same.

"Refused" means throw `IllegalStateException`. "Unchanged" means return the
state you were given. Switch over the state; `State` is sealed, so no
`default` is needed.

**Examples**

- `open(new Closed())` -> `Opened[]`
- `lock(new Closed(), "1234")` -> `Locked[code=1234]`
- `unlock(new Locked("1234"), "1234")` -> `Closed[]`
- `open(new Locked("1234"))` -> throws `IllegalStateException`
