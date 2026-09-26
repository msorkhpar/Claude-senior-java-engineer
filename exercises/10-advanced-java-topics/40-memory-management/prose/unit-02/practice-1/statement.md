Mark-and-sweep in two phases, as on the page. **Mark**: starting from the GC
roots, follow references and mark every object reached; **an already marked
object is skipped**, which is how cycles end. **Sweep**: free every unmarked
object and **clear the marks of survivors** for the next collection. So
**reachability from roots decides, not reference counts**: two objects that
point at each other but that no root reaches are both garbage.

Write `Heap`, a simulation over named objects:

- `allocate(name)` adds an object (a name already used is refused with
  `IllegalArgumentException`); `reference(from, to)` makes `from` point to `to`.
- `addRoot(name)` / `removeRoot(name)`.
- `collect()` runs mark and sweep and returns the names it freed, in allocation
  order; freed objects are gone from `live()` (also in allocation order).

| setup | `collect()` | `live()` |
|---|---|---|
| A, B, C, D; root A; A->B, B->D | `[C]` | `[A, B, D]` |
| X, Y; X->Y, Y->X; no roots | `[X, Y]` | `[]` |
| root A; A->B, B->A | `[]` | `[A, B]` |
| then `removeRoot(A)`, `collect()` | `[A, B]` | `[]` |
