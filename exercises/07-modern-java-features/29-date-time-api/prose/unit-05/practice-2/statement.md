Contract terms arrive as a `Period` built by other code, so they may look like
`Period.of(0, 14, 3)`: fourteen months and three days. Write
`Terms.describe(Period term)`, a label for the term:

- Twelve months are shown as a year: 14 months is `1 year, 2 months`.
- **Days are never turned into months** (nor into years): 45 days is `45 days`,
  and 400 days is `400 days`.
- **Mixed signs are balanced**: 2 years and minus 3 months is `1 year, 9 months`.
- Parts that are zero are left out; one of a unit is singular.
- **A term of nothing is `0 days`.**

The parts are joined with `, ` in the order years, months, days. The whole term
is never negative.

| term | answer |
|---|---|
| `Period.of(0, 14, 3)` | `1 year, 2 months, 3 days` |
| `Period.of(2, 1, 1)` | `2 years, 1 month, 1 day` |
| `Period.ofMonths(6)` | `6 months` |
| `Period.ofDays(45)` | `45 days` |
| `Period.of(2, -3, 0)` | `1 year, 9 months` |
| `Period.ZERO` | `0 days` |
