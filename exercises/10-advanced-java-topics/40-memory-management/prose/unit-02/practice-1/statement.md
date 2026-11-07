The checkout service traces every request: `SpanTable` keeps a span per request object, and the
web layer calls `event(request, ...)` while it handles it. Since the tracer went in, the heap
grows by a request body for every request served, and a heap dump shows finished requests still
reachable from the tracer. An exporter also keeps finished spans for a while, to send them in
batches, and needs their labels then.

`SpanTable` already records events and labels spans. Finish the two `TODO`s in
`src/main/java/practice/SpanTable.java`:

- `newTable()`: the map from each request to its span;
- `open(request)`: a new span labelled `"<method> <path>"`, for example `"POST /orders"`.

When you're done, the tracer holds a span for as long as the web layer holds its request, and
not after; a span the exporter still holds gives its label without keeping its request in memory.
About four lines in total.

The tests ask the JVM for collections with `System.gc()` and wait, up to ten seconds, for a
finished request to be collected.
