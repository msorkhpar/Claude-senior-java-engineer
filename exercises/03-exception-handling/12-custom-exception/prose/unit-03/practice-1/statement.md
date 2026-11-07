An online shop's checkout books a shipment as its last step. The shipping module refuses some
orders on purpose, and its team adds new kinds of refusal every few releases without telling the
checkout team. Last month one of those new refusals reached the customer as "500 Internal Server
Error". The month before, a `NullPointerException` inside the shipping code was shown to
customers as "We can't ship this order right now", and nobody was paged for a week.

Finish `placeOrder` in `src/main/java/practice/Checkout.java`:

- a booked shipment answers with its tracking number;
- a postcode the carrier doesn't serve gets `ADDRESS_ANSWER`;
- every other refusal from the shipping module, today's and the ones added later, gets
  `LATER_ANSWER`;
- anything else that goes wrong reaches the shop's error handler unchanged.

About six lines in total. The tests bring a refusal of their own that the checkout has never seen.
