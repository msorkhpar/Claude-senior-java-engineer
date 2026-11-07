The checkout service reserves stock for each line of an order before it takes payment. On Black
Friday the reservations started failing, and the alert channel filled with
`ReservationFailedException` lines that nobody could act on: the on-call engineer couldn't tell
which product had failed, or whether the stock database had rejected a lock, timed out or run out
of connections. The checkout service itself only needs the SKU and the quantity, to tell the
customer which line couldn't be reserved.

Finish the two `TODO`s in `src/main/java/practice/Reservations.java`:

- the constructor of `ReservationFailedException`;
- `reserve(...)`, which takes the stock from the store.

When you're done, a failed reservation tells the checkout service which line failed, and the log
line and stack trace of the exception tell the on-call engineer the same, plus everything the
database said. About eight lines in total.
