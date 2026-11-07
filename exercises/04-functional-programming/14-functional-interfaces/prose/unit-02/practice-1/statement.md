The FX service prices every request from a rate card it fetches from an upstream that answers
slowly and allows a handful of requests a minute. The card is wrapped in one shared
`Supplier<RateCard>` that all request threads call. After a deploy the upstream rejected the
service for a minute: the first burst of requests had each fetched the card. Some desks have no
card at all, and their source answers `null`. When the upstream is briefly down at startup, the
service must keep trying on later requests rather than stay broken, and it must start without
calling the upstream.

Write `get()` in `src/main/java/practice/Once.java`, adding the fields it needs. When you're done,
the source runs once for all threads, on the first request, whatever it returns, and a load that
throws is tried again by the next caller. About twelve lines in total.

The tests call `get()` from a thread pool, and hold a load in progress with a latch while a second
caller arrives.
