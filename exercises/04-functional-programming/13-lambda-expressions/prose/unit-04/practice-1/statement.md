The exchange-rate service keeps rates in memory in front of a rates API that bills every call.
This month's invoice from the API provider lists a call for every lookup the service made,
including the many for pairs it already had. A profile of the same service, running with debug
output switched off, shows it spending noticeable time copying and sorting its cache.

Finish the two `TODO`s in `src/main/java/practice/RateService.java`:

- `cachedOrFetched(pair)`: the cached rate, or one fetched from the API and cached when there is
  none. `cached` and `fetchAndCache` already exist.
- `trace(pair, rate)`: a debug line with the pair, its rate and `cacheSnapshot()`.

When you're done, each pair costs one API call, and a service with debug output off never takes a
snapshot. About two lines in total.
