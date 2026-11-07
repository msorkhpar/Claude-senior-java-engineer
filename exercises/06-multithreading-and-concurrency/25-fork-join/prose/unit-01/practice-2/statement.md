Every night at two the shop refreshes the price of each SKU from the rate service, a network call
of a few hundred milliseconds per SKU. Since the refresh went live, the checkout's address
validation, which runs on `CompletableFuture.supplyAsync`, times out for the twenty minutes the
refresh takes.

Finish the two `TODO`s in `src/main/java/practice/PriceRefresh.java`:

- `lookUpAll(skus)`: the prices, looked up several SKUs at a time;
- `refresh(skus)`: runs `lookUpAll` so that the refresh uses `workers` threads of its own and
  shares none with the rest of the service; it closes the pool it creates when it's done.

About seven lines in total. The tests use a rate service that answers at once, and check which
threads ran the lookups.
