The checkout prices a basket by asking a partner's quote service for each item's price:
`GET {base}/quotes/{symbol}` answers `200` with the price as a plain decimal, such as `318.40`.
`QuoteClient` runs on the checkout's request threads and shares the service's one `HttpClient`,
which is built with a two-second connect timeout.

Two incidents last month:

- the partner's servers kept accepting connections during an overload but stopped answering;
  within minutes every checkout thread was waiting on a quote, and the shop stopped responding;
- during the partner's maintenance window, and for every product the partner doesn't list,
  checkouts failed with a `NumberFormatException` from deep in the pricing code, which the
  checkout's error handling doesn't expect.

Finish the two `TODO`s in `src/main/java/practice/QuoteClient.java`: `request(...)` and
`priceIn(...)`. A lookup must end within the budget the client was given, with a price or with an
`IOException`, and only a real quote may become a price. About six lines in total.

The tests run the partner as a small HTTP server on 127.0.0.1; nothing leaves the machine.
