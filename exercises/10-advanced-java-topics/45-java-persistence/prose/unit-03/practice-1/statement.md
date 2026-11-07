The box office of a small theatre sells seats from four counters, each with its own database
connection. Last Friday, show 12 had two seats left; two counters each sold both, and four people
arrived for two seats. On another night a customer was told "sold out" at one counter, and the next
counter's sale of that show hung for seconds and then failed.

`hold(...)` in `src/main/java/practice/SeatHolds.java` already reads, decides and writes in one
transaction. Finish its two `TODO`s:

- `seatsLeft(...)`: the number the hold decides on;
- `refuse(...)`: what happens when there are too few seats.

When you're done, two counters that ask for the same last seats at the same moment never both get
them, and a refused customer never slows down the next one. A customer who can't get seats is told
`false`, never an exception. About eight lines in total.

The tests run each counter on its own connection to an in-memory H2 database (table
`shows(id, seats_left)`), and use the `Pause` to let a second counter in between the first
counter's read and its write.
