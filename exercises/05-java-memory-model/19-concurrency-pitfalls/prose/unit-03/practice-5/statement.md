Double-checked locking needs `volatile` and care. The page's preferred
alternative is the **Initialization-on-Demand Holder** idiom: the instance is a
`static final` field of a private nested `Holder` class. The JLS initializes
that class exactly once, thread-safely, and only when `getInstance()` first
touches it, so the singleton is lazy with no lock and no `volatile`.

Write the singleton `Registry`:

- `getInstance()` returns the one `Registry`, to every thread.
- The constructor is private and counts each instance it builds.
- `instancesCreated()` returns that count. Calling it (or anything else on
  the class) must **not** create the instance: creation happens on the first
  `getInstance()` only.

| calls, in a fresh JVM | `instancesCreated()` |
|---|---|
| `instancesCreated()` | `0` |
| `getInstance()` from 8 threads at once | `1`, and all 8 got the same object |
