A retail chain writes a price file every night, one `sku;price` line per product, and every
store imports it; stores sometimes fix a price by hand and import the file again. Since the
nightly job moved to a server in Lyon, no store has been able to import the file. Before that, a
hand-edited file once put a product on the tills at a price that no discount rule or price check
would touch.

`write` and `read` already handle the lines. Finish the two `TODO`s in
`src/main/java/practice/PriceFile.java`:

- `formatPrice(price)`: the price with two decimals, the same on every store's server;
- `parsePrice(field)`: the price a field holds, or an `IllegalArgumentException` when the field
  holds no usable price.

About four lines in total.
