Each instance of the pricing service warms its caches at startup, each cache on a thread of its
own, and tells the load balancer it is ready only when every loader has finished. A cache that
can't be loaded doesn't stop the instance: it starts without that cache and logs which ones are
missing.

The gate that holds the startup thread back has failed twice. One instance never reported ready,
although its log showed all three caches loaded, the first of them within a few milliseconds. On
the day the tax-rules service was down, no instance reported ready at all.

Finish the two `TODO`s in `src/main/java/practice/StartupGate.java`:

- `runLoader(...)`: each loader's thread runs its loader through it;
- `awaitLoaded(...)`: the startup thread waits there, up to its timeout.

When you're done, the startup thread gets `true` as soon as every loader has finished, however
early they finished and whether or not they failed, and `false` if its time runs out first. About
eight lines in total; you may add fields.
