The checkout service converts prices with exchange rates that a loader thread reads from storage
once, at startup. Requests arrive from the first second, often before the rates do. A request
must then wait for them, but not for ever: after the configured timeout it fails with a
`TimeoutException`, and the load balancer retries it on another node. The loader reads into a
scratch map, which it clears and reuses once it has handed the rates over.

Finish the two `TODO`s in `src/main/java/practice/RatesCache.java`:

- `onLoaded(...)`, called once on the loader's thread;
- `awaitLoaded()`, called by every request before it reads the rates.

When you're done, no request answers from rates that aren't fully loaded, a request that comes
early waits and then answers, and one that has waited the whole timeout fails with
`TimeoutException`. The `rates` field stays a plain field. About five lines in total.

The tests load the rates and make requests on threads of their own, and hold the loader in the
middle of its work.
