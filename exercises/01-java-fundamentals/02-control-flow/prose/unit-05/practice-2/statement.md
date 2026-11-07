The shop gives customers a window, in days after delivery, to send an order back. The window
depends on the reason they pick:

| Reason | Days |
|---|---|
| `DAMAGED`, `WRONG_ITEM` | 60 |
| `NOT_AS_DESCRIBED`, `LATE_DELIVERY` | 30 |
| `CHANGED_MIND` | 14 |

The `Reason` enum is generated from the returns service's schema, and that team adds a reason a
few times a year. Last spring they added one, and for three weeks customers who chose it were
quoted a window that nobody in this team had agreed to, until finance noticed.

Write `windowDays(...)` in `src/main/java/practice/RefundPolicy.java`, without touching the enum.
When the next reason arrives, two things must hold:

- once this project is built against the new enum, the build fails, with the compiler pointing at
  your method;
- a copy of this class that was built before the reason existed, and meets it in production,
  throws rather than quoting any window.

About five lines in total. The tests compile your file themselves, once as it is and once with a
reason added, and run the old build against the new enum.
