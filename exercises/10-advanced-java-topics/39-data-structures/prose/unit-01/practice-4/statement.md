A carrier charges by weight tier: each tier starts at a weight in grams and covers every parcel up
to the start of the next one. `ShippingFees` holds the table, and two methods are missing.
Finance found parcels charged a tier too low, and the rate-change report has been leaving out
tiers.

Finish the two `TODO`s in `src/main/java/practice/ShippingFees.java`:

- `feeFor(grams)`: the fee of the tier the parcel falls into; a parcel lighter than every tier
  is refused with `IllegalArgumentException`;
- `tierStartsBetween(fromGrams, toGrams)`: the tier starts from `fromGrams` to `toGrams`, both
  included, lightest first.

When you're done, every weight, including one exactly at a tier's start, is charged its own tier,
and the report lists every tier start in the range, including one at either end. About four lines
in total.
