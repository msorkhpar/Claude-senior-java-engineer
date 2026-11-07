The storefront's `StockClient` used to run its lookups on a pool of 16 platform threads. Since it
moved to one virtual thread per lookup, a busy page asks the warehouse's inventory API for
hundreds of SKUs at once, and the API, which accepts 6 calls at a time, answers most of them with
`429 Too Many Requests`. The storefront then shows "out of stock" for items on the shelf.

Lookups already work. Finish the `TODO` in `src/main/java/practice/StockClient.java`:
`callInventory(...)` must never let the API see more than `maxConcurrentCalls` calls in progress,
whatever happens to the calls and the threads making them. You may add a field. About six lines in
total.

When you're done, the API is never over its limit, SKUs the client already knows are answered
straight away even while the API is busy, and a lookup that fails, or whose caller is interrupted,
doesn't change how many calls can be in progress.
