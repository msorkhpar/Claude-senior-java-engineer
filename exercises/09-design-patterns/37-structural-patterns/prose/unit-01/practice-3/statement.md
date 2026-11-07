The pricing service is moving to `CompletableFuture`, but its FX rates still come from a legacy
client that takes a listener and answers later, on a thread of its own. A pricing request combines
several rates with `thenCombine` and turns any failure into a "price unavailable" reply with
`exceptionally`. Those requests must always get a reply: last quarter, a handler that waited on a
rate nobody would ever deliver held its thread until the pool ran dry.

`Rates` declares the client's interfaces (don't change them) and `RateUnavailableException`.
Finish `rate(pair)` in `src/main/java/practice/Rates.java`, so that it returns at once with a
future that settles however the client's lookup ends: with the rate, or failed with a
`RateUnavailableException` that carries the client's reason, or failed with whatever the client
threw. Callers handle every failure through the future. About ten lines in total.
