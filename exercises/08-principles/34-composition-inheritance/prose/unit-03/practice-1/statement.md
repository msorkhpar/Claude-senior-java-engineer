The ticketing service keeps seat holds in `SeatHolds`. The box office shares one store between
its request threads, so it gets a `ConcurrentHashMap`; the overnight re-seating tool runs on one
thread over a `TreeMap`; the tests use a `HashMap`. The callers never see which: they get a
store from `shared()`, `sorted()` or `plain()` and call its methods.

Last month a bug reached production that every test had passed: code that worked over the
`HashMap` failed in the box office. Make sure that can't happen again. Finish the three `TODO`
methods in `src/main/java/practice/SeatHolds.java`:

- `hold(seat, guest)`: the guest now holds the seat;
- `holder(seat)`: who holds the seat, if anyone;
- `holds()`: every hold at this moment.

When you're done, a caller that works with one of the three stores works the same way with the
other two, including when it passes `null` and when it changes the store while walking the list
it got from `holds()`. `release` is already written. About four lines in total.
