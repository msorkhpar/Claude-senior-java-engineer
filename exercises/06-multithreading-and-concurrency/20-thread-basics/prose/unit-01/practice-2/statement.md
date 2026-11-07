Purchasing compares prices before every order: `QuoteBoard.cheapest(...)` asks each supplier's
API for a quote and picks the lowest. Each API can take a few seconds, so the suppliers are asked
at the same time, each on a thread of its own. A supplier that doesn't stock the item answers
`null` and is left out.

Last quarter a buyer ordered 600 brackets at the "cheapest" price, and it turned out that the
supplier with the lowest price had been failing for a week: its answers never reached the
comparison, and nothing in the logs said why.

`cheapest(...)` already compares the offers. Finish the two `TODO`s in
`src/main/java/practice/QuoteBoard.java`:

- `ask(...)`: start asking one supplier and return at once with something to collect the
  answer from later;
- `answer(...)`: collect it.

When you're done, the check still asks every supplier at the same time, each on a thread named
`quote-<supplier>`, and a supplier that fails makes the whole check fail with `QuoteFailed`,
whose cause is exactly what that supplier threw. About eight lines in total.

The tests use suppliers that answer at once, fail, or wait for one another.
