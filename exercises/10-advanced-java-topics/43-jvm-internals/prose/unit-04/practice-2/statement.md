The page's monitoring code reads each memory pool's `MemoryUsage` and prints
it. A `MemoryUsage` has `init`, `used`, `committed` and `max`; **`max` is -1
when the pool has no defined maximum** (Metaspace, for one). *Committed* is
what the JVM has reserved from the OS, *max* is the ceiling the pool may grow
to.

Write `MemoryReport` with:

- `line(name, usage)`: `Pool [<name>]: used=<u>MB, max=<m>MB`, where sizes
  are **whole mebibytes** (bytes / 1,048,576, rounded down) and an undefined
  maximum prints as `max=-1MB`.
- `usedPercent(usage)`: `100 * used / max` as an `OptionalDouble`, measured
  **against `max`**, and empty when `max` is undefined.

| used | committed | max | `line("G1 Old Gen", u)` | `usedPercent(u)` |
|---|---|---|---|---|
| 100 MiB | 512 MiB | 512 MiB | `Pool [G1 Old Gen]: used=100MB, max=512MB` | `19.53125` |
| 1,572,863 B | 2 MiB | 4 MiB | `...used=1MB, max=4MB` | `37.49997615814209` |
| 30 MiB | 32 MiB | -1 | `...used=30MB, max=-1MB` | empty |
