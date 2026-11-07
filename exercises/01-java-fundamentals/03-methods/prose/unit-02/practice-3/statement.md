A web shop prices its basket through `PriceBook`. Prices the shop has on file come from a map; for
anything else it asks the supplier's quote service, a slow remote call the supplier bills per
request. Buyers add products the shop has never listed, and the catalogue team lists new products
before their price is settled.

`PriceBook` already stores the listings. Finish the two `TODO`s in
`src/main/java/practice/PriceBook.java`:

- `price(sku)`: the price on file for a product, if it has one;
- `priceOrQuote(sku)`: the price on file, or else the supplier's quote.

When you're done, any product a buyer can name gets an answer without an exception, and the
supplier's bill shows a request only for products whose price isn't on file. About two lines in
total.
