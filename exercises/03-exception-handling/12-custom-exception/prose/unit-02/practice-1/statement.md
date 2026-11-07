The airline's app lets passengers change seats after check-in. Support has three kinds of tickets
about it. Some passengers who picked a seat number from an old seat plan, one this aircraft
doesn't have, were left with no seat at all. Twice, two passengers ended up holding the same
seat, after one of them picked a seat that showed as free on a page loaded a minute earlier. And
one passenger "moved" out of a seat that belonged to someone else.

Write `rebook(...)` in `src/main/java/practice/SeatMap.java`. When you're done, a seat change
either happens completely or is refused with an `IllegalArgumentException` whose message names
the seat at fault, and a refused request leaves every seat exactly as it was. About ten lines.

`assign(...)`, which check-in uses, is already written.
