The shop ships with several carriers, and each carrier's rates come from a slow lookup: a
carrier's API, or a file. `Shipping.CachedRateCard` is the base class for them, and
`Shipping.ParcelPost` is the first carrier. The freight team will add theirs next month, as their
own subclass, and the last time they did, two things went wrong: their card was created with the
currency `"usd"`, and every invoice it produced was refused by the bank; and once both carriers had
quoted the same zone, the checkout page showed Parcel Post's price for freight.

Finish the two `TODO`s in `src/main/java/practice/Shipping.java`, both in `CachedRateCard`:

- the constructor: no carrier's card, today's or one written later, can exist with a currency that
  isn't an ISO 4217 code (three capital letters); refuse one with `IllegalArgumentException`;
- `rateCents(zone)`: the card's rate for the zone, looking each zone up at most once for that card.

About six lines in total. The tests add a carrier of their own, the way the freight team will.
