The page's interview question: *how would you implement a thread-safe getter and
setter for a counter?* A getter and a mutator are also where you decide how the
field is shared between threads.

Write `ThreadSafeCounter`:

- `void incrementCount()` adds one;
- `int getCount()` returns the current count.

Many threads may call `incrementCount()` at the same time, and **no increment may
be lost**. Keep in mind that `count++` is a read, an add and a write: three steps,
not one.

Examples:

```
ThreadSafeCounter c = new ThreadSafeCounter();
c.getCount()                                      -> 0
c.incrementCount(); c.incrementCount(); c.incrementCount();
c.getCount()                                      -> 3
8 threads x 100000 incrementCount(), then getCount()  -> 800000
```
