The cart, the search page and the order service all ask the catalog for prices, and unknown SKUs
are frequent: old links, typos, a search that tries SKUs as the user types. Two complaints reached
the team last week. The order service started sending a SKU that never existed, and the error
log, which records each failure with its stack trace, pointed at the search page, which had asked
for that SKU first, that morning. And the search page, which calls `find(...)` on every keystroke,
spends most of its CPU on SKUs that don't exist.

Finish the two `TODO`s in `src/main/java/practice/Catalog.java`:

- `unknown(...)`: the exception `price(...)` throws for a SKU the catalog doesn't sell. It names
  the SKU, and its trace shows the code that asked, even when another caller asked for the same
  SKU before or after.
- `find(...)`: the price, or nothing, for the search page.

About four lines in total.
