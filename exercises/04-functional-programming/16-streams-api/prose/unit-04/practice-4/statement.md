A grocery app prices a basket against the store's price table and hands the list of prices to the
checkout, the receipt and the analytics services. A product with no price yet must show up as a
gap (`null`) in its place, not break the basket. Last month the analytics service appended a
discount to the list it was given, and the receipt printed it as an item; nobody may change the
list again.

Finish the two `TODO`s in `src/main/java/practice/PriceLookup.java`:

- `pricesOf(...)`: one price per code, in order, `null` where the table has none;
- `labelsFor(...)`: the codes that are not discontinued, as the `String[]` the shelf printer takes.

About two lines in total.
