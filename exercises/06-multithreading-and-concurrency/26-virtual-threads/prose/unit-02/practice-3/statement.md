The page's point about compatibility: `java.util.concurrent` works unchanged on
virtual threads. A `BlockingQueue` producer-consumer pipeline is written exactly
as before; a virtual thread waiting in `take()` just unmounts instead of holding
an OS thread.

Write `Pipeline.run(List<String> items, Function<String, String> stage, int workers)`:

- One **producer** virtual thread puts every item on a `BlockingQueue`.
- `workers` **worker** virtual threads, running at the same time, take items
  from the queue and pass each through `stage`.
- When the items run out, **every** worker stops (a common way: the producer
  puts one "poison pill" per worker after the items; compare it by identity).
- `run` returns all the stage results (in any order) only once the producer and
  all workers have finished, however long a stage call takes.

| items | stage | workers | answer (any order) |
|---|---|---|---|
| `["a", "b", "c", "d"]` | `String::toUpperCase` | 1 | `["A", "B", "C", "D"]` |
| `["x", "y", "z"]` | each call waits until 3 calls are in progress | 3 | `["x", "y", "z"]` |
| `["a"]` | `s -> s` | 3 | `["a"]`, and `run` returns |
| `["slow"]` | `s -> s + "!"`, blocked for a while | 2 | `["slow!"]`, only after the call ends |
