The billing service runs in a data centre in Frankfurt and bills customers from Los Angeles to
Auckland. Customers have started writing in: a payment made at nine in the evening in California
shows up on an invoice dated the next day, a Tokyo customer's daily statement lists payments from
two different days, and the "today's statement" page shows tomorrow for customers in Brazil
around midnight.

Finish the three `TODO` methods in `src/main/java/practice/Billing.java`:

- `invoiceDate(...)`: the date printed on an invoice for a payment;
- `startOf(...)`: the moment a customer's day begins, which `statement(...)` uses for both ends
  of the day;
- `today(...)`: the customer's current date.

When you're done, every date a customer sees is a date in their own calendar, wherever the
service runs, and `today(...)` answers from the `Clock` that `Billing` was built with. About three
lines in total.
