A subscription starts on a date and renews every `term`, a `Period` such as one
month or one year. "One month" is a calendar amount: from January 15 it is 31
days, from February 15 it is 29 days in 2024.

Write `Subscription.renewals(LocalDate start, Period term, int count)`. It returns
the first `count` renewal dates, in order. Renewal number `k` is `k` terms after
`start`.

| start | term | count | answer |
|---|---|---|---|
| `2024-01-15` | `P1M` | 3 | `[2024-02-15, 2024-03-15, 2024-04-15]` |
| `2024-01-15` | `P3M` | 2 | `[2024-04-15, 2024-07-15]` |
| `2024-01-15` | `P1M15D` | 2 | `[2024-03-01, 2024-04-14]` |
| `2024-01-31` | `P1M` | 1 | `[2024-02-29]` |
| `2024-01-31` | `P1M` | 3 | `[2024-02-29, 2024-03-31, 2024-04-30]` |
| `2024-02-29` | `P1Y` | 4 | `[2025-02-28, 2026-02-28, 2027-02-28, 2028-02-29]` |

A month that is too short for the start's day renews on its **last** day: it
never spills into the following month. And a short month does not pull the
later renewals earlier: the customer who started on January 31 renews on March
31, not March 29.

A `count` of 0 gives an empty list.
