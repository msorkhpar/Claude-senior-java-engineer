The page's pitfall: every single call on a `ConcurrentHashMap` is atomic, but
`get` followed by `put` is **two** atomic calls, not one. Two threads can both
read the old count and both write the same new count, and one visit is lost.
The page's Q5 shows exactly this lost update on a counter.

Write `Visits.record(ConcurrentHashMap<String, Integer> counts, String page)`.
It adds one to `page`'s count, starting from 1 when the page is not in the map
yet. Many threads call it at the same time on one map, and no visit may be
lost.

| counts before | calls | counts after |
|---|---|---|
| `{home=1000}` | `record(counts, "home")` twice | `{home=1002}` |
| `{}` | `record(counts, "about")` | `{about=1}` |
| `{home=1000}` | two threads each call `record(counts, "home")` at once | `{home=1002}` |
