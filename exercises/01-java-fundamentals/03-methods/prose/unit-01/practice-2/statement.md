The checkout looks up the customer by email and the price of every item before it takes payment.
Since the last release the customer table grows by one row on every visit of a returning
customer, and the bill for the catalogue service shows one call per item in every basket, as if
the price cache weren't there.

Finish `customerId(...)` and `price(...)` in `src/main/java/practice/Checkout.java`. When you're
done, a customer's row is inserted on their first visit only, and the catalogue is asked for each
SKU once, after which the cache answers. About two lines in total.
