A shoe warehouse stocks every shoe as two SKUs, one per foot, and `StockLedger` counts the units
on hand. Its pairing report lists the pairs whose left and right counts differ, so that someone
goes and finds the missing shoes. Two problems reached the team: the stock report crashes as soon
as it is asked about a SKU that hasn't arrived yet, and the big distribution centre's pairing
report flags pairs that a manual count found complete.

`report` and `unevenPairs` already work. Finish the two `TODO`s in
`src/main/java/practice/StockLedger.java`:

- `onHand(sku)`: the units on hand, for any SKU;
- `sameStock(a, b)`: whether the two SKUs hold the same number of units, whatever that number is.

About two lines in total.
