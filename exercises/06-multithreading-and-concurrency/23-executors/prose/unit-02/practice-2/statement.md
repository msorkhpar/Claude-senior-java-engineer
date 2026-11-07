A product page shows a price from the supplier's pricing API, asked on a pool that every product
page shares. When the supplier is slow, the page is meant to show the last price we cached rather
than wait. Last month the supplier stopped answering for one product line. Its pages showed the
cached prices for a few minutes, as designed, and then every product page on the site, including
those of other product lines, started showing cached prices too.

`priceOf(...)` already asks the supplier. Finish the `TODO` in
`src/main/java/practice/PriceService.java`:

- `priceOrCached(...)`: what the page gets back from the call it started.

When you're done, a page shows the supplier's price if it comes within the budget and the cached
price otherwise, a supplier that fails gets the cached price too, and a supplier that stops
answering doesn't keep the pool's threads from the pages after it. About eight lines in total.

The tests run the pages on a real one-thread pool. Their supplier gives up when its thread is
interrupted, as `HttpClient.send` does.
