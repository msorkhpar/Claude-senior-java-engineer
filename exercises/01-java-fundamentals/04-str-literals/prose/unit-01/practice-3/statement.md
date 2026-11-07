The Nordic shop loads its supplier's price list, four million products, into memory every night.
A heap dump shows four million `"SEK"` strings and four million `"SE"` strings, one of each per
product, each its own object. Every SKU and every product name is different; currencies and
countries come from a handful of values.

The loader already has a table, `codes`, which it drops with the catalogue when the next price list
replaces it, and `tableSize()` reports its size to the shop's metrics. Finish the `TODO`s in
`src/main/java/practice/CatalogueLoader.java`:

- `shared(value)`: the one string the loader keeps for `value`'s text;
- `parse(line)`: the product for one `sku;name;currency;country` line.

About four lines in total.
