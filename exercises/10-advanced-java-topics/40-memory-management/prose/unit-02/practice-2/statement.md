Generational collection rests on the weak generational hypothesis: most objects
die young. New objects start in **Eden**; a **minor GC collects only the young
generation** (Eden and the survivor space), and each object that survives it
gets one year older and moves to the survivor space. A survivor older than the
**tenuring threshold** is promoted to the **old generation**, which only a
**major GC** examines: **a major GC collects the whole heap**.

Write `GenerationalHeap` (the enum `Generation { EDEN, SURVIVOR, OLD }` is given):

- `new GenerationalHeap(threshold)`: **the tenuring threshold is a setting, not a
  constant**.
- `allocate(id)` puts a new object in `EDEN` with age 0.
- `minorGc(reachable)`: each young object not in `reachable` is freed; each
  one in it gets `age + 1`, and moves to `SURVIVOR`, or to `OLD` when its new
  age is above the threshold. Old objects are not touched. Returns the freed
  ids in allocation order.
- `majorGc(reachable)`: frees every unreachable object in any generation;
  survivors keep their generation and age. Returns the freed ids.
- `generation(id)` and `age(id)`; an unknown id (for example a freed one)
  throws `NoSuchElementException`.

| threshold 3, `obj` reachable | after minor GC 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|
| `generation("obj")` | SURVIVOR | SURVIVOR | SURVIVOR | OLD | OLD |
