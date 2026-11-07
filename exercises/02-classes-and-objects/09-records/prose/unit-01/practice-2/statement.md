The listing service pages through products with a `PageRequest(int page, int size)`. Requests come
from query parameters, from saved searches (which store only the page), and from `next()` when a
client scrolls on. The database has started receiving `OFFSET -2147483648`, and a client that asks
for `size=5000` gets a page of 5,000 rows, which the service then caches under a key that no
request at the limit ever hits.

Finish the two `TODO`s in `src/main/java/practice/PageRequest.java`:

- the compact constructor: no request may exist with a page below 0 or a size below 1 (refuse
  them with `IllegalArgumentException`), and a size above `MAX_SIZE` is served as `MAX_SIZE`;
- `fromQuery(page, size)`: reads the two query parameters; without a size the client gets
  `DEFAULT_SIZE`.

When you're done, an impossible request can't be made by any route, and a request for more than
`MAX_SIZE` rows is the same request as one for exactly `MAX_SIZE`. About eight lines in total.
