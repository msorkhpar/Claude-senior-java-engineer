The quote service protects its pricing backend: while `limit` quotes are in flight it refuses new
ones with `BusyException`. Yesterday the backend was down for four minutes. It came back, but the
service went on refusing every quote until it was restarted, with nothing running, and the
gateway, which retries a quote that failed with an `IOException`, never retried anything that
morning.

Finish `lookup(...)` in `src/main/java/practice/QuoteService.java`. It counts the quote in
`inFlight` for as long as it runs, answers from the cache when it can, and otherwise asks the
backend and caches the price.

When you're done, `inFlight()` goes back down however a quote ends: with a price, from the
cache, or with whatever the backend throws, checked or not. And the backend's exception reaches
the caller of `quote()` as it was thrown. About ten lines in total.
