The theatre's box office sells seats from many request threads at once, and each sale charges the
buyer's card through a payment gateway that takes a second or two. The first version charged two
buyers for the same seat on the night of a premiere. Its replacement never double-charged, but
during a sell-out every buyer in the queue waited for every charge ahead of them. And once, after
the gateway timed out, a seat stayed unsellable until the service was restarted.

Write `book(...)` in `src/main/java/practice/SeatMap.java`. It sells a free seat to the holder of
the card and returns `true`; it returns `false` when the seat isn't free or the card is declined,
and lets an exception from the gateway reach the caller. With many buyers at once, no seat is
charged to two buyers, a booking never waits for the charge of a different seat, and a seat whose
charge failed in any way can be sold again. `state(...)` reports `FREE`, `HELD` or `SOLD`. About
twelve lines in total.

The tests use a gateway that can hold a charge open, decline a card, or throw.
