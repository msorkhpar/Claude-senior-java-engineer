The shop's quote service creates one `QuoteFormatter` at startup, and every request thread uses
it. Since the last release, support gets a ticket every few days: a customer's PDF quote shows
lines or a total that belong to another customer's quote, made at about the same time. Nobody has
reproduced it in a test.

Price lookups go to the pricing service and can take seconds, so a quote often stops halfway
through while other quotes are rendered.

Write `render(...)` in `src/main/java/practice/QuoteFormatter.java`. It returns one line per SKU,
in the order given, built with `line(sku, cents)`, then `line("TOTAL", sum)`. When you're done,
two quotes rendered at the same time each come out with only their own lines and their own total,
and no quote waits for another one's slow price. About eight lines in total.

The tests stop one quote inside a slow price lookup and render another while it waits.
