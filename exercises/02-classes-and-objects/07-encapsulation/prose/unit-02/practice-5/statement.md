The page's interview question: *how would you implement a thread-safe getter and
setter for a counter?* A getter and a mutator are also where you decide how the
field is shared between threads.

`count++` is three steps, not one: read the count, add one, write it back. To make
that visible, `ThreadSafeCounter` is given a `step` (a `Runnable`) in its
constructor. The no-argument constructor gives a step that does nothing.

Write:

- `void incrementCount()`: read the count, run `step.run()` exactly once, then
  write back the count read plus one;
- `int getCount()`: the current count.

Many threads may call `incrementCount()` at the same time, and **no increment may
be lost**, even when the step pauses one thread between its read and its write.

Examples:

```
ThreadSafeCounter c = new ThreadSafeCounter();
c.getCount()                                         -> 0
c.incrementCount(); c.incrementCount(); c.incrementCount();
c.getCount()                                         -> 3
two threads, one incrementCount() each, both pausing in step  -> getCount() is 2
```
