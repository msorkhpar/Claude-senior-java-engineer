Checkout converts card payments in foreign currencies to euros at the treasury service's rates.
Last month the US dollar moved sharply one afternoon, and finance found dollar payments converted
at the morning's rate for more than an hour. The old rate class has been deleted, and its
replacement is half written.

Two rules come from the treasury team:

- a payment is never converted at a rate fetched more than ten minutes before it (`MAX_AGE`);
- the treasury accepts at most one call per ten minutes from this service, and one call to
  `allRates()` returns every currency it quotes.

Finish `rate(...)` in `src/main/java/practice/ExchangeRates.java` so that both hold, with as little
machinery as they allow. Take the time from the `clock` you are given, never from the system
clock: the tests move it by hand. The class has one caller thread. About eight lines in total,
fields included.
