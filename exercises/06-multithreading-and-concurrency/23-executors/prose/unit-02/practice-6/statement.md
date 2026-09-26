The page contrasts `Future.get()`, which blocks, with `CompletableFuture`, whose
chain does not: `supplyAsync(...)` starts the work, `thenApply(...)` transforms
its result when it arrives, and `exceptionally(...)` turns a failure into a
value.

Write `Pipeline.run(executor, fetch, process, fallback)`, returning at once a
`CompletableFuture<String>` that:

- runs `fetch` **on `executor`**;
- applies `process` to what `fetch` returned;
- completes with `fallback` instead if `fetch` or `process` throws.

`run` itself must not wait for any of it.

| `fetch`, `process` | the future completes with |
|---|---|
| `() -> "data"`, `String::toUpperCase` | `"DATA"` |
| a fetch that waits for a signal | not done when `run` returns; `"DATA"` after the signal |
| `() -> Thread.currentThread().getName()` on an executor whose thread is `io-1`, `s -> s` | `"io-1"` |
| `() -> { throw new IllegalStateException("offline"); }`, fallback `"cached"` | `"cached"` |
| `() -> "data"`, a `process` that throws, fallback `"cached"` | `"cached"` |
