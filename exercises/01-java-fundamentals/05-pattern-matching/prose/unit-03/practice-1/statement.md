The warehouse prints one label for every order event it receives. Customers fill in a billing
address, a shipping address, both or neither, and the order system passes along `null` for any
address that was left empty. Since the label printer was moved to record patterns, some orders
come back as "not an order" and others are labelled for the wrong address.

Finish `shipTo(...)` in `src/main/java/practice/ShippingLabels.java`:

- a parcel goes to the customer's shipping address, or to the billing address when there is no
  shipping address on file; use `label(...)` for the line;
- a customer with no address at all collects the parcel at the counter: `pickup(...)`;
- anything that is not an `Order`, `null` included, is rejected with `IllegalArgumentException`.

The records, `label` and `pickup` are written. About six lines in total.
