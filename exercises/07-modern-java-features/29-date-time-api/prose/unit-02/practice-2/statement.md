A car park's barrier controller prints a ticket at the entry, gives every car 15 free minutes,
and lets drivers pay for more time at a machine. The team wants to test the night-shift rules
without waiting for the night, so the controller now hands `CarPark` the `Clock` it runs on, and
the tests hand it one they move by hand.

Finish the three `TODO` methods in `src/main/java/practice/CarPark.java`:

- `enter(...)`: the ticket printed when a car drives in;
- `overstayed(...)`: whether the car has stayed past the time its ticket covers;
- `pay(...)`: the ticket after the driver pays for more time.

When you're done, every time `CarPark` uses is the time of the clock it was built with, and a
driver who pays is covered for the extra time. About four lines in total.
