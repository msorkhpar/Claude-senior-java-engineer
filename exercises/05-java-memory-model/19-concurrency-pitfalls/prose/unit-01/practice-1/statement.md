The page's `UnsynchronizedCounter` loses updates: `counter++` is a read, an add and
a write, and two threads can interleave them. Its first fix, `SynchronizedCounter`,
makes **both** methods `synchronized`: the lock gives mutual exclusion to the
writers and visibility to the readers.

Write `HitCounter`, a page-hit counter that follows that fix. `hit()` adds one hit,
and `hits()` returns the total so far. Guard the count with the counter object's own
monitor (`synchronized` methods, or `synchronized (this)`), for the write **and** the
read.

| calls | `hits()` |
|---|---|
| a new counter | `0` |
| `hit()` three times | `3` |

The tests hold the counter's lock themselves and check that your `hit()` and
`hits()` wait for it.
