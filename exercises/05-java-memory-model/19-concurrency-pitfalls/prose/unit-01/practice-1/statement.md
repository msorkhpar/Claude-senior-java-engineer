An airline keeps seat holds for each flight in one `ConcurrentHashMap` from seat to customer.
Every booking request builds its own `SeatHolds` over the flight's map, so requests for the same
seat run on different threads at the same moment. Two support tickets came in this week:

- "Both my wife and I got a confirmation for 31F. At the gate the system had only her name."
- "I held 5B, then someone else's cancellation freed my seat. They had clicked Release twice."

`holderOf(...)` already works. Finish the two `TODO`s in `src/main/java/practice/SeatHolds.java`:

- `reserve(seat, customer)`: true when this customer now holds the seat;
- `release(seat, customer)`: true when this customer's hold was removed.

When you're done, two requests that race for one free seat give it to exactly one customer, a
refused request changes nothing, and a release that arrives late never frees someone else's seat.
About two lines in total.

The tests use a map that pauses one request right after it reads, while another request runs.
