Before planned maintenance, `OutageNotifier` tells the customers of a region, by every channel they
gave us. Two complaints came in after the last outage: the mail gateway rejected a batch because one
of its addresses was `null`, and customers were woken at night although they had set quiet hours.

Finish the two `TODO` methods in `src/main/java/practice/OutageNotifier.java`:

- `contacts(segment)`: each customer's email, then phone, customer by customer. Either may be
  missing. The CRM returns `null`, not an empty list, for a region with no customers;
- `isQuietHour(quietHours, hour)`: whether `hour` is one of the customer's quiet hours.

About five lines in total. When you're done, nothing `null` reaches the gateway, an empty region
sends nothing without failing, and nobody hears from us during their quiet hours.
