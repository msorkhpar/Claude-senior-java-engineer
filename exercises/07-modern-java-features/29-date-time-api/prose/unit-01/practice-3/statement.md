A `java.util.Date` is **mutable**: anyone holding a reference can call
`setTime` and change it. A class that keeps a `Date` it was handed, or hands
out the `Date` it keeps, lets outside code change its state behind its back.
Where the `Date` type cannot be replaced (an old API still uses it), the page's
fix is a **defensive copy**.

Write `Booking`, which must keep its start moment no matter what callers do
with their `Date` objects:

- `Booking(Date start)` records the start.
- `Date getStart()` returns the start.

| steps | `getStart().getTime()` |
|---|---|
| `new Booking(new Date(1_710_505_800_000L))` | `1710505800000` |
| keep the `Date` you passed in, call `setTime(0)` on it | still `1710505800000` |
| call `getStart().setTime(0)` | still `1710505800000` |
