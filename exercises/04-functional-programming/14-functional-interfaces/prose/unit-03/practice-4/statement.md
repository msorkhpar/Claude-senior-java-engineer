The shipyard's planning tool asks the same slow questions over and over: what a hull section costs
(its own work plus the section it is welded onto), who owns a berth (a call to another service that
answers `null` for a berth nobody has leased). `Memo` wraps such a function so each key is computed
once, and the tool already uses it everywhere.

Finish the `TODO` in `src/main/java/practice/Memo.java`: `apply(key)` answers from the cache, or
computes the answer once and remembers it. The function you wrap may call the same `Memo` for other
keys while it computes, and a `null` answer is remembered like any other. You may change the
declaration of the cache. About seven lines.
