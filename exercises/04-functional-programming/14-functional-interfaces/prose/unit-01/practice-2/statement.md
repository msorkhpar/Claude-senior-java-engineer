`Consumer.andThen(after)` builds a consumer that runs `this` first and `after` second, handing **both the same
input**. Chaining is how a pipeline of side effects (log, then store, then notify) is put together from small steps.

Write `ConsumerChain.chain(List<Consumer<T>> steps)`. It returns one `Consumer<T>` that, on `accept(t)`, runs every
step in list order on `t`.

| steps | `accept("Alice")` records |
|---|---|
| log, store | `["log:Alice", "store:Alice"]` |
| store, log | `["store:Alice", "log:Alice"]` |
| log | `["log:Alice"]` |

The list may contain `null` entries and may be empty; the returned consumer must be usable either way. Think about
what should happen to the remaining steps when one of them throws.
