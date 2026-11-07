The warehouse team needs the value of its stock: for every SKU, the quantity on hand times the
unit price, summed, plus the number of SKUs that are out of stock. A big warehouse has a few
million SKUs, and each price is a lookup in the price list, so the valuation runs on a
`ForkJoinPool`. One `StockValuation` is created at startup and shared: the nightly report uses it,
and so does the "value now" button on the warehouse screen, which people press at any time,
including while the nightly report is running.

Finish `Totals.plus` and `compute()` of the `Value` task in
`src/main/java/practice/StockValuation.java`. When you're done:

- every valuation returns the value and the out-of-stock count of the SKUs it was given, including
  for an empty warehouse and for values far beyond 21 million euros;
- a valuation gives the same totals the second time it is asked, and while another one runs.

About fifteen lines in total. The tests run real pools of two and three workers.
