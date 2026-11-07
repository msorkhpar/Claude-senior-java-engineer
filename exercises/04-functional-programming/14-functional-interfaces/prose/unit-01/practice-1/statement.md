The shop's checkout runs one consumer for every order it accepts. That consumer takes the stock,
emails the customer, and, where the deployment has one, writes the order to a compliance log.
Support has two complaints: customers get "your order is on its way" for orders that were then
refused because the warehouse was out of stock, and the staging deployment, which has no
compliance log, can't be started with the current code.

Write `build(reserve, confirm, audit)` in `src/main/java/practice/OrderPipeline.java`. The steps
are given to you; you decide how they are put together. When you're done:

- a customer is emailed only about an order whose stock was reserved;
- when the stock can't be reserved, the checkout gets that exception as it was thrown, so it can
  show the customer why;
- the compliance log holds only orders whose customer was emailed;
- a deployment that passes `null` for `audit` still takes orders.

About three lines in total.
