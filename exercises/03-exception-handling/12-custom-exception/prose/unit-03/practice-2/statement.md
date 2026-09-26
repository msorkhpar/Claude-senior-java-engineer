Reuse a standard exception when it describes the error; create a custom one
when the error belongs to your domain and callers need to tell it apart, or
when it must carry data. Write `SeatMap` with seats numbered `1` to `capacity`:

- `SeatMap(int capacity)`, `boolean isReserved(int seat)`, `void close()`;
- `void reserve(int seat) throws SeatTakenException`, and its failures:

| situation | exception | message |
|---|---|---|
| seat is not in `1..capacity` | `IllegalArgumentException` | `No such seat: <seat>` |
| the map was closed with `close()` | `IllegalStateException` | `Booking closed` |
| the seat is already reserved | `SeatTakenException` (checked, yours to complete) with `getSeat()` | `Seat <seat> is taken` |

When more than one failure applies to a call, which one is reported is up to
you; the tests give each call exactly one.

`reserve(3)` on a fresh `new SeatMap(10)` reserves seat 3; a second
`reserve(3)` throws `SeatTakenException` with `getSeat() == 3`.

Each failure has a different cause: a bug in the caller, an object in the wrong
state, or a business rule a caller can recover from. Choose the type that says
which.
