Adding months to a date keeps its day of month when it can. When the target
month is too short, `java.time` **clamps** the day to that month's last day:
`2024-01-31` plus one month is `2024-02-29`. Clamping loses information, so
month arithmetic is not reversible, and chaining it is not the same as adding
the months at once: January 31, plus one month, plus one month is March 29,
but January 31 plus two months is March 31.

A subscription is billed monthly on the day of its first bill. Write `Billing`:

- `static LocalDate nthBill(LocalDate first, int n)` returns the date of bill
  number `n`, where bill `0` is `first`.
- `static List<LocalDate> schedule(LocalDate first, int count)` returns the
  dates of bills `0` to `count - 1`, in order.

Every bill falls on the first bill's day of month, or on the month's last day
when the month is too short for it.

| call | answer |
|---|---|
| `nthBill(2024-01-15, 13)` | `2025-02-15` |
| `nthBill(2024-01-31, 1)` | `2024-02-29` |
| `schedule(2024-01-31, 4)` | `[2024-01-31, 2024-02-29, 2024-03-31, 2024-04-30]` |
