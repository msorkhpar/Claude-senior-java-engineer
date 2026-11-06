The shop's checkout asks for a shipping rate on every basket, hundreds of times a second, and the
rates change a few times a day. `RateCache` keeps them in memory behind a
`ReentrantReadWriteLock`. The tariff job's `replace` is already written. What's missing is the
read: answer from the cache, and on a zone that isn't cached yet, ask the slow rates service once,
keep the answer and return it.

Write `rateFor` in `src/main/java/practice/RateCache.java`. Checkouts must be able to read rates
side by side without waiting for each other, and a miss must load the rate without stopping the
cache. About twelve lines.

The tests make each call from a thread of their own, and hold the read lock themselves to stand in
for a checkout that is in the middle of a read.
