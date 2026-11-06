On the night a big concert goes on sale, the box office re-syncs its seat map every few minutes,
and each re-sync keeps the map locked for several seconds. Customers clicking a seat during a
re-sync see a spinner until their browser gives up, and the ticket site's error rate goes through
the roof. The product owner's ruling: a hold request gets an answer within its budget, even if the
answer is "try again in a moment".

Write `hold` in `src/main/java/practice/SeatMap.java`. It does the hold with the `holdLocked`
method that is already there, under the map's lock, and answers what `holdLocked` answers. If the
map stays locked for longer than the budget passed to the constructor, it answers `BUSY` and
changes nothing. A re-sync that ends within the budget must not cost the customer the seat. About
six lines.

The tests hold the map's lock themselves to stand in for a re-sync, and send each request from a
thread of its own.
