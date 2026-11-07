The edge service caches geo-IP answers by address so it doesn't call the lookup provider for
every request. Memory allows a fixed number of answers. When a new one arrives and the cache is
full, the answer that has gone longest without being *used* must go: an address read a second ago
stays, even if it was first cached long before the others. Answers arrive one at a time, and at
start-up a warm-up job loads the previous run's answers in one `putAll`; the bound must hold
either way.

`RecentLookups.java` extends `LinkedHashMap`, and its constructor already passes
`orderedByUse()` as the third argument of `LinkedHashMap(int, float, boolean)` (see that
constructor's javadoc). Finish the three `TODO` methods, about four lines in total:

- `orderedByUse()`: the order that makes "the eldest entry" the least recently used one;
- `removeEldestEntry`: keeps the cache at no more than `capacity` answers;
- `nextToDrop()`: the address that would be dropped next, or `null` when the cache is empty.
