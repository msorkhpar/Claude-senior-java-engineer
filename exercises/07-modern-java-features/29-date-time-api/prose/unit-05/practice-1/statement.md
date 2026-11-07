A pharmacy chain puts each filled prescription aside for the customer and sends a reminder every
evening until it is picked up. Customers choose the time of their reminder, say 19:15, and the
complaints start every March: the reminders arrive at 20:15. The other rule comes from the
regulator: a prescription is kept for 24 hours and then goes back on the shelf, and inspectors
time it.

`PickupReminders` already builds the reminder list and answers whether a hold has run out.
Finish the two `TODO`s in `src/main/java/practice/PickupReminders.java`:

- `nextReminder(last)`: the reminder after `last`;
- `releasedAt(heldAt)`: when a prescription put aside at `heldAt` goes back on the shelf.

Both keep the zone they were given. The pharmacies are in several countries, so the tests use
zones whose clocks change on different dates. About two lines in total.
