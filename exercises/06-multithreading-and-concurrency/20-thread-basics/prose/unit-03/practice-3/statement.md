The page's first answer to "how do you handle `InterruptedException`?" is
**propagate it**: library code that can declare `throws InterruptedException`
should let it reach the caller, who decides what an interrupt means. Never an
empty `catch`, and never a wrapper that hides what happened.

Write `Fetcher.fetchAll(List<Fetch> fetches)`, which declares
`throws InterruptedException`. `Fetch` is given: `String get() throws
InterruptedException`, a blocking call.

- Run the fetches one after another, in order, and return their results in that
  order.
- Before starting each fetch, check whether the current thread has been
  interrupted; if it has, clear the flag and throw `InterruptedException`, as a
  blocking call would.
- If a fetch throws `InterruptedException`, let that same exception reach the
  caller, and start no further fetch.

| fetches | answer |
|---|---|
| `"a"`, `"b"`, `"c"` | `["a", "b", "c"]` |
| `"a"`, then one that throws `InterruptedException`, then `"c"` | that `InterruptedException`; the third fetch never runs |
| any, from a thread already interrupted | `InterruptedException`; no fetch runs |
