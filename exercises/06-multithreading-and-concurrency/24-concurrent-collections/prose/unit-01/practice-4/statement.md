The fare search keeps the fares on sale for a route, in cents, in a `ConcurrentSkipListSet` that
pricing threads change all the time. The search page has two filters. "From 64.00 to 110.00"
must show a fare of exactly 110.00. "Under 40.00" must not show a fare of exactly 40.00. A result
the page has already shown must not change underneath it while the customer scrolls.

Finish `between(min, max)` and `under(limit)` in `src/main/java/practice/FareIndex.java`. Both
return the matching fares lowest first, as they are when the method runs. About two lines in
total.
