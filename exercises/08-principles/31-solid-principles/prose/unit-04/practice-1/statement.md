The shop keeps one live price book and freezes a copy at every month-end, so that finance can
replay that month's orders at that month's prices. In `PriceBooks`, a `FrozenPriceBook` is a
`PriceBook` like the live one, and its `setPrice` throws.

Last month-end the repricing job was started against the frozen March book instead of the live
one. The wiring compiled and passed review, and the job failed at run time on the first price it
tried to change. Finish `src/main/java/practice/PriceBooks.java` so that:

- `FrozenPriceBook.of(source)` freezes the prices the source answers at that moment; repricing
  the source later doesn't reach the frozen book;
- handing a frozen book to `Repricer` is a compile error, not a run-time one;
- finance's `Checkout` can still be built on a frozen book.

Leave `Repricer` and the three interfaces as they are. About eight lines in total.
