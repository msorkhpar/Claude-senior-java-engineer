The web shop quotes shipping with the carrier's parcel types, `Letter`, `Box` and `Tube`. Prices
in cents:

| Parcel | Price |
|---|---|
| a `Letter` of up to 100 g | 140 |
| a heavier `Letter` | 290 |
| a `Box` | 650, plus 120 for each started kilogram, plus 400 if it is fragile |
| a `Tube` up to 90 cm long | 980 |
| a longer `Tube` | 1,500 |

`Parcel` and its records come from the carrier's library, and the carrier adds a kind of parcel
once or twice a year. The last time, the shop quoted the new kind at a price nobody had agreed,
for a month, at a loss on every one.

Write `priceCents(...)` in `src/main/java/practice/ParcelPricing.java`, without touching the
hierarchy. When the next kind arrives, two things must hold:

- once the shop is built against the new library, the build fails, with the compiler pointing at
  your method;
- a copy of the shop built before the new kind existed, which meets one in production, throws
  rather than quoting any price.

About five lines in total. The tests compile your file themselves, once as it is and once with a
kind added, and run the old build against the new hierarchy.
