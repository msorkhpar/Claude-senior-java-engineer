The admin console lets operators run operations on live service objects by name: `resize` with
an `int` on a thread pool, `restart` on a worker, `drain` (a private method) before a deploy. A
profile of the console under a load test shows most of its time in `Class.getDeclaredMethod`,
called once per request, although the console only knows a few dozen operations.

Finish the two `TODO`s in `src/main/java/practice/OperationCache.java`:

- `method(...)`: the method a request needs, ready to invoke;
- `key(...)`: what the method is cached under.

When you're done, each operation is looked up on its first request only, every request still runs
the method it names, and private operations run. About eight lines in total.

The tests count the lookups through the `Finder` the constructor takes.
