`Parcels` holds a sealed interface `Parcel` permitting three records, which are
implicitly final: `Letter(int grams)`, `Box(int kilograms)` and
`Tube(int centimetres)`.

Write `static int rate(Parcel p)` as one `switch` over the parcel, with no
`default`:

| parcel | rate |
|---|---|
| a letter up to and including 20 g | 1 |
| any heavier letter | 2 |
| a box over 30 kg | 50 (freight) |
| any other box | 10 + 1 per kilogram |
| a tube | 8 |

A rule that holds only for some parcels of a kind is a **guarded** case
(`case Box b when ...`). It must come before the plain case for the same
kind, and the plain case is still needed, or the switch would not cover every
box and would no longer be exhaustive.

**Examples**

- `rate(new Letter(15))` -> `1`
- `rate(new Letter(50))` -> `2`
- `rate(new Box(5))` -> `15`
- `rate(new Box(31))` -> `50`
