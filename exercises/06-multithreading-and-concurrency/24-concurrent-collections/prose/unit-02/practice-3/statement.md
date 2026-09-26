The page's key point 7: a thread-safe collection does **not** make a
check-then-act sequence atomic. `if (!map.containsKey(k)) map.put(k, v)` is two
calls, and another thread can put `k` between them. The page's fix is a single
atomic method on `ConcurrentHashMap`.

Write `Seats.claim(ConcurrentHashMap<String, String> seats, String seat, String person)`.
If `seat` is free, `person` takes it. If it is taken, nothing changes. Either
way it returns whoever holds the seat after the call. Two threads claiming the
same free seat at the same moment must agree on one winner, and that winner
must be the one in the map.

| seats before | call | answer | seats after |
|---|---|---|---|
| `{}` | `claim(seats, "A1", "ann")` | `"ann"` | `{A1=ann}` |
| `{A1=ann}` | `claim(seats, "A1", "bob")` | `"ann"` | `{A1=ann}` |
| `{}` | `claim(seats, "A1", "ann")` and `claim(seats, "A1", "bob")` at once | the same name from both | `{A1=` that name `}` |
